package com.rcs.newsletter.core.service.util;

import static com.rcs.newsletter.NewsletterConstants.CONFIRMATION_LINK_TOKEN;
import static com.rcs.newsletter.NewsletterConstants.CONFIRMATION_UNREGISTER_LINK_TOKEN;
import static com.rcs.newsletter.NewsletterConstants.FIRST_NAME_TOKEN;
import static com.rcs.newsletter.NewsletterConstants.LAST_NAME_TOKEN;
import static com.rcs.newsletter.NewsletterConstants.LIST_NAME_TOKEN;
import static com.rcs.newsletter.NewsletterConstants.NEWSLETTER_BUNDLE;
import static com.rcs.newsletter.NewsletterConstants.ONLINE_ARTICLE_LINK;
import static com.rcs.newsletter.NewsletterConstants.ONLINE_NEWSLETTER_CONFIRMATION_PAGE;
import static com.rcs.newsletter.NewsletterConstants.ONLINE_NEWSLETTER_VIEWER_PAGE;
import static com.rcs.newsletter.NewsletterConstants.TEMPLATE_TAG_BLOCK_CLOSE;
import static com.rcs.newsletter.NewsletterConstants.TEMPLATE_TAG_BLOCK_OPEN;
import static com.rcs.newsletter.NewsletterConstants.TEMPLATE_TAG_CONTENT;
import static com.rcs.newsletter.NewsletterConstants.TEMPLATE_TAG_TITLE;
import static com.rcs.newsletter.NewsletterConstants.UNDEFINED;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringEscapeUtils;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.model.JournalArticleDisplay;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.rcs.newsletter.core.model.NewsletterCategory;
import com.rcs.newsletter.core.model.NewsletterMailing;
import com.rcs.newsletter.core.model.NewsletterSubscription;
import com.rcs.newsletter.core.model.NewsletterSubscriptor;
import com.rcs.newsletter.core.model.NewsletterTemplate;
import com.rcs.newsletter.core.model.NewsletterTemplateBlock;
import com.rcs.newsletter.core.model.commons.TemplateBlockComparator;

public class EmailFormat {

    private static Log log = LogFactoryUtil.getLog(EmailFormat.class);
    //Bundle Keys
    //private static final String TEMPLATE_BLOCK_EMPTY_SELECTOR = "newsletter.admin.mailing.template.select.article";
    private static final String DEFAULT_REPLACEMENT_USER_TOKEN = "newsletter.admin.mailing.default.replacement.user";
    
    /**
     * To determine if the content is personalizable or not
     * @param content
     * @return 
     */
    public static boolean contentPersonalizable(String content) {
        boolean result = false;
        if (content.contains(CONFIRMATION_LINK_TOKEN)
                || content.contains(LIST_NAME_TOKEN)
                || content.contains(FIRST_NAME_TOKEN)
                || content.contains(LAST_NAME_TOKEN)
                || content.contains(ONLINE_ARTICLE_LINK)) {
            result = true;
        }
        return result;
    }

    /**
     * Replace special tags with user information
     * @param content
     * @param subscription
     * @param themeDisplay
     * @return 
     */
    public static String replaceUserInfo(String content, NewsletterSubscription subscription, ThemeDisplay themeDisplay) {
        return replaceUserInfo(content, subscription, themeDisplay, null);
    }

    /**
     * Replace special tags with user information Including the OnLine Article Viewer
     * @param content
     * @param subscription
     * @param themeDisplay
     * @param articleId
     * @return 
     */
    public static String replaceUserInfo(String content, NewsletterSubscription subscription, ThemeDisplay themeDisplay, Long archiveId) {
        ResourceBundle serverMessageBundle = ResourceBundle.getBundle(NEWSLETTER_BUNDLE, themeDisplay.getLocale());

        //Default Replacement information
        String subscriptorFirstName = serverMessageBundle.getString(DEFAULT_REPLACEMENT_USER_TOKEN);
        String subscriptorLastName = "";
        String categoryName = "";
        String confirmationLinkToken = "";
        String confirmationUnregisterLinkToken = "";
        String onlineArticleLink = "";

        String portalUrl = themeDisplay.getPortalURL();
        if (subscription != null) {
            //Replace Subscriptor Information
            NewsletterSubscriptor subscriptor = subscription.getSubscriptor();
            subscriptorFirstName = subscriptor.getFirstName() != null ? subscriptor.getFirstName() : "";
            subscriptorLastName = subscriptor.getLastName() != null ? subscriptor.getLastName() : "";

            //Replace Category Information
            NewsletterCategory category = subscription.getCategory();
            categoryName = category.getName();

            //Replace Confirmation Link Information
            StringBuilder confirmationLinkBuilder = new StringBuilder(portalUrl);
            confirmationLinkBuilder.append(ONLINE_NEWSLETTER_CONFIRMATION_PAGE);
            confirmationLinkBuilder.append("?subscriptionId=");
            confirmationLinkBuilder.append(subscription.getId());
            confirmationLinkBuilder.append("&activationkey=");
            confirmationLinkBuilder.append(subscription.getActivationKey());

            confirmationLinkToken = confirmationLinkBuilder.toString();

            //Replace UNREGISTER Confirmation Link Information            
            StringBuilder unregisterStringBuilder = new StringBuilder(portalUrl);
            unregisterStringBuilder.append(ONLINE_NEWSLETTER_CONFIRMATION_PAGE);
            unregisterStringBuilder.append("?unsubscriptionId=");
            unregisterStringBuilder.append(subscription.getId());
            unregisterStringBuilder.append("&deactivationkey=");
            unregisterStringBuilder.append(subscription.getDeactivationKey());

            confirmationUnregisterLinkToken = unregisterStringBuilder.toString();

            //Replace Online Viewer Link Information
            if (archiveId != null) {
                StringBuilder onlineViewerStringBuilder = new StringBuilder(portalUrl);
                onlineViewerStringBuilder.append(ONLINE_NEWSLETTER_VIEWER_PAGE);
                onlineViewerStringBuilder.append("?nlid=");
                onlineViewerStringBuilder.append(archiveId);
                onlineViewerStringBuilder.append("&sid=");
                onlineViewerStringBuilder.append(subscription.getId());

                onlineArticleLink = onlineViewerStringBuilder.toString();
            }

        }

        content = content.replace(FIRST_NAME_TOKEN, subscriptorFirstName);
        content = content.replace(LAST_NAME_TOKEN, subscriptorLastName);
        content = content.replace(LIST_NAME_TOKEN, categoryName);
        content = content.replace(CONFIRMATION_LINK_TOKEN, confirmationLinkToken);
        content = content.replace(CONFIRMATION_UNREGISTER_LINK_TOKEN, confirmationUnregisterLinkToken);
        content = content.replace(ONLINE_ARTICLE_LINK, onlineArticleLink);

        return content;
    }

    /**
     * Fix the relative Paths to Absolute Paths on images
     */
    public static String fixImagesPath(String emailBody, ThemeDisplay themeDisplay) {
        String siteURL = getUrl(themeDisplay);
        String result = emailBody.replaceAll("src=\"/", "src=\"" + siteURL);
        result = result.replaceAll("src='/", "src='" + siteURL);
        result = result.replaceAll("&amp;", "&");
        return result;
    }

    /**
     * Returns the base server URL
     */
    public static String getUrl(ThemeDisplay themeDisplay) {
        StringBuilder result = new StringBuilder();
        String[] toReplaceTmp = themeDisplay.getURLHome().split("/");
        for (int i = 0; i < toReplaceTmp.length; i++) {
            if (i < 3) {
                result.append(toReplaceTmp[i]);
                result.append("/");
            }
        }
        return result.toString();
    }

    /**
     * 
     * @param u
     * @return
     * @throws Exception 
     */
    public static File getFile(URL u) throws Exception {
        URLConnection uc = u.openConnection();
        InputStream raw = uc.getInputStream();
        InputStream is = new BufferedInputStream(raw);
        
        String contentType = uc.getContentType() != null ? uc.getContentType() : URLConnection.guessContentTypeFromStream(is);
        int contentLength = uc.getContentLength();
        if ((contentType != null && contentType.startsWith("text/")) || contentLength == -1) {
            throw new IOException("Problem with URL.");
        }
        
        File tmp = null;
        OutputStream output = null;
        try {
            log.info("ContenType: " + contentType);
            String fileExt = "";
            if (contentType.endsWith("png")) {
                fileExt = ".png";
            } else if (contentType.endsWith("jpg")) {
                fileExt = ".jpg";
            } else if (contentType.endsWith("jpeg")) {
                fileExt = ".jpeg";
            } else if (contentType.endsWith("jpe")) {
                fileExt = ".jpe";
            } else if (contentType.endsWith("gif")) {
                fileExt = ".gif";
            }
            tmp = File.createTempFile("image", fileExt);
            output = new FileOutputStream(tmp);
            int val;
            while ((val = is.read()) != -1) {
                output.write(val);
            }
        } catch (IOException e) {
            log.error(e);
        } finally {
            try {
                is.close();
                output.flush();
                output.close();
            } catch (Exception e) {
                log.error(e);
            }
        }
        return tmp;
    }

    /**
     * Get the message with attached images
     * @param fromIA
     * @param toIA
     * @param subject
     * @param content
     * @return
     * @throws Exception 
     */
    public static MailMessage getMailMessageWithAttachedImages(InternetAddress fromIA, InternetAddress toIA, String subject, String content) throws Exception {

        ArrayList<String> images = getImagesPathFromHTML(content);

        MailMessage message = new MailMessage(fromIA, toIA, subject, content, true);

        // embed the images into the multipart
        for (int i = 0; i < images.size(); i++) {
            String image = (String) images.get(i);
            URL imageUrl = null;
            try {
                String imagePathOriginal = (String) images.get(i);
                log.debug("Original image url: " + imagePathOriginal);
                String imagePath = StringEscapeUtils.unescapeHtml(image);
                imagePath = imagePath.trim().replaceAll(" ", "%20");
                imageUrl = new URL(imagePath);
                File tempF = getFile(imageUrl);//To Improve probably add Cache
                content = StringUtils.replace(content, imagePathOriginal, "cid:" + tempF.getName());
                message.addFileAttachment(tempF);
            } catch (Exception ex) {
                log.error("Problem with image url: " + image, ex);
            }
        }
        message.setBody(content);

        return message;
    }

    /**
     * Method imported from COPS (com.rcs.community.common.MimeMail)
     * Returns an ArrayList with all the different images paths. Duplicated paths are deleted.
     *
     * NB! the returned images urls may have html encoding included.
     * 
     * @param htmltext a String HTML with content
     * @return an ArrayList with the images paths
     */
    public static ArrayList<String> getImagesPathFromHTML(String htmltext) {
	
        ArrayList<String> imagesList = new ArrayList<String>();
        try {
            // get everything that is inside the <img /> tag
            String[] imagesTag = StringUtils.substringsBetween(htmltext, "<img ", ">");

            if (imagesTag != null) { // if there are images
                for (int i = 0; i < imagesTag.length; i++) {
                    // get what is in the src attribute
                    String imagePath = StringUtils.substringBetween(imagesTag[i], "src=\"", "\"");
                    if (imagePath == null) {
                        imagePath = StringUtils.substringBetween(imagesTag[i], "src='", "'");
                    }

                    log.debug("Image path to process: " + imagePath);
                    
                    if (!imagesList.contains(imagePath)) { // don't save the duplicated images
                        imagesList.add(imagePath);
                    }
                }
            }

            // And now for the background images only one style of typing is allowed for now!!!
            imagesTag = StringUtils.substringsBetween(htmltext, "background=\"", "\"");
            if (imagesTag != null) {
                for (int i = 0; i < imagesTag.length; i++) {
                    // get what is in the src attribute
                    String imagePath = imagesTag[i].trim();
                    log.info("Processing: " + imagePath);
                    if (!imagesList.contains(imagePath)) { // don't save the duplicated images
                        imagesList.add(imagePath);
                    }
                }
            }
            imagesTag = StringUtils.substringsBetween(htmltext, "background=\'", "\'");
            if (imagesTag != null) {
                for (int i = 0; i < imagesTag.length; i++) {
                    // get what is in the src attribute
                    String imagePath = imagesTag[i].trim();
                    log.info("Processing: " + imagePath);
                    if (!imagesList.contains(imagePath)) { // don't save the duplicated images
                        imagesList.add(imagePath);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error in getImagesPathFromHTML: ", ex);
        }
        return imagesList;
    }

    /**
     * Get the email content based on the template
     * @param mailing
     * @param themeDisplay
     * @return 
     */
    public static String getEmailFromTemplate(NewsletterMailing mailing, ThemeDisplay themeDisplay) throws Exception {
        log.info("Executing getEmailFromTemplate in EmailFormat");
        NewsletterTemplate template = mailing.getTemplate();
        String result = template.getTemplate();
        String fTagBlockOpen = fixTagsToRegex(TEMPLATE_TAG_BLOCK_OPEN);
        String fTagBlockClose = fixTagsToRegex(TEMPLATE_TAG_BLOCK_CLOSE);
        String fTagBlockTitle = fixTagsToRegex(TEMPLATE_TAG_TITLE);
        String fTagBlockContent = fixTagsToRegex(TEMPLATE_TAG_CONTENT);

        log.info("***********************************************************");
        log.info("**************************************** template original:");
        log.info(result);
        log.info("***********************************************************");
        log.info("***********************************************************");
        
        result = result.replace(TEMPLATE_TAG_BLOCK_OPEN, fTagBlockOpen).replace(TEMPLATE_TAG_BLOCK_CLOSE, fTagBlockClose).replace(TEMPLATE_TAG_TITLE, fTagBlockTitle).replace(TEMPLATE_TAG_CONTENT, fTagBlockContent);
        String resulttmp = new String(result);

        List<NewsletterTemplateBlock> ntbAll = mailing.getBlocks();
        
        log.info("all blocks " + ntbAll.size());
        
        // FIX (remove null blocks until we fix mapping problem)
        List<NewsletterTemplateBlock> ntb = new ArrayList<NewsletterTemplateBlock>();
        for(NewsletterTemplateBlock b :ntbAll){
            if (b != null){
                ntb.add(b);
            }
        }
        
        Collections.sort(ntb, new TemplateBlockComparator());
        Pattern patternBlock = Pattern.compile(fTagBlockOpen + ".*?" + fTagBlockClose, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = patternBlock.matcher(result);
        String toReplaceTmp = "";
        int count = 0;

        //Iterate each Block
        while (m.find()) {
            try {
                toReplaceTmp = resulttmp.substring(m.start() + fTagBlockOpen.length(), m.end() - fTagBlockClose.length());
                
                //If there is a content related to this block
                if (ntb.size() > count) {
                    if (ntb.get(count).getArticleId() != null && ntb.get(count).getArticleId() != UNDEFINED) {
                	String localeId = themeDisplay.getLocale().getLanguage();
                        JournalArticle ja = JournalArticleLocalServiceUtil.getLatestArticle(themeDisplay.getDoAsGroupId(),ntb.get(count).getArticleId().toString());
			String content = JournalArticleLocalServiceUtil.getArticleContent(ja, ja.getTemplateId(), null, localeId, themeDisplay);
                        log.info("Block: " + content);
                        
                        toReplaceTmp = toReplaceTmp.replace(fTagBlockTitle, ja.getTitle(localeId));                        
                        toReplaceTmp = toReplaceTmp.replace(fTagBlockContent, content);

                        resulttmp = m.replaceFirst(StringUtils.replace(toReplaceTmp, "$",  "\\$"));

                        //If there is a NOT content related to this block the block is deleted
                    } else {
                        resulttmp = m.replaceFirst("");
                    }

                    //If there is a NOT content related to this block the block is deleted
                } else {
                    resulttmp = m.replaceFirst("");
                }
                m = patternBlock.matcher(resulttmp);

            } catch (PortalException ex) {
                log.error("Error while trying to read article", ex);
            } catch (SystemException ex) {
                log.error("Error while trying to read article", ex);
            }
            count++;
        }
        log.info("***********************************************************");
        log.info("******************************************* template final:");
        log.info(resulttmp);
        log.info("***********************************************************");
        log.info("***********************************************************");
        return resulttmp;
    }

    /**
     * Get the edit code that allow the selection of articles on each block
     * @param templateContent
     * @return 
     */
    public static String parseTemplateEdit(String incomingTemplateContent, String newsletterArticleType, String newsletterArticleCategory, String newsletterArticleTag, ThemeDisplay themeDisplay) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        log.debug("Executing parseTemplateEdit in EmailFormat");
        String result = "";

        //ResourceBundle newsletterMessageBundle = ResourceBundle.getBundle(NEWSLETTER_BUNDLE, themeDisplay.getLocale());
        //String emptySelectorMessage = newsletterMessageBundle.getString(TEMPLATE_BLOCK_EMPTY_SELECTOR);
        String fTagBlockOpen = fixTagsToRegex(TEMPLATE_TAG_BLOCK_OPEN);
        String fTagBlockClose = fixTagsToRegex(TEMPLATE_TAG_BLOCK_CLOSE);
        String templateContent = validateTemplateFormat(incomingTemplateContent);
        
        //If the template contains at least one block with one title or content
        if (templateContent != null && !templateContent.isEmpty()) {
            String templateContentTmp = templateContent;
            Pattern patternBlock = Pattern.compile(fTagBlockOpen + ".*?" + fTagBlockClose, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher m = patternBlock.matcher(templateContent);

            int count = 0;

            //Get all newsletter articles to create the selectors
            HashMap<String, JournalArticle> resultArticleNewsletter = new HashMap<String, JournalArticle>();
            try {
                //Search Articles by Type
                List<JournalArticle> articlesByType = ArticleUtils.findArticlesByType(newsletterArticleType);
                for (JournalArticle article : articlesByType) {
                    if (!resultArticleNewsletter.containsKey(article.getArticleId())) {
                        resultArticleNewsletter.put(article.getArticleId(), article);
                    }
                }
                //Search Articles by Category
                List<JournalArticle> articlesByCategory = ArticleUtils.findArticlesByCategory(newsletterArticleCategory);
                for (JournalArticle article : articlesByCategory) {
                    if (!resultArticleNewsletter.containsKey(article.getArticleId())) {
                        resultArticleNewsletter.put(article.getArticleId(), article);
                    }
                }
                //Search Articles by Tag
                List<JournalArticle> articlesByTag = ArticleUtils.findArticlesByTag(themeDisplay, newsletterArticleTag);
                for (JournalArticle article : articlesByTag) {
                    if (!resultArticleNewsletter.containsKey(article.getArticleId())) {
                        resultArticleNewsletter.put(article.getArticleId(), article);
                    }
                }
            } catch (SystemException ex) {
                log.error("Could not filter the articles by this category, type, or tag", ex);
            } catch (PortalException ex) {
                log.error("Could not filter the articles by this category, type, or tag", ex);
            }
            List<JournalArticle> newsletterArticles = new ArrayList<JournalArticle>(resultArticleNewsletter.values());

            //Create HTML select option with all newsletter articles
            StringBuilder selectHTMLOptionsSB = new StringBuilder("");
            //StringBuilder selectHTMLOptionsSB = new StringBuilder("<option value=\"");
            //selectHTMLOptionsSB.append(String.valueOf(UNDEFINED));
            //selectHTMLOptionsSB.append("\">");
            //selectHTMLOptionsSB.append(emptySelectorMessage);
            //selectHTMLOptionsSB.append("</option>");
            for (JournalArticle journalArticle : newsletterArticles) {
                selectHTMLOptionsSB.append("<option value=\"");
                //selectHTMLOptionsSB.append(journalArticle.getId());
                selectHTMLOptionsSB.append(journalArticle.getArticleId());
                selectHTMLOptionsSB.append("\">");
                selectHTMLOptionsSB.append(journalArticle.getTitle());
                selectHTMLOptionsSB.append("</option>");
            }

            //Iterate each Block and replace by the HTML select
            
            while (m.find()) {
                
                StringBuilder selectHTMLItemSB = new StringBuilder("<div id=\"blockSelector");
                selectHTMLItemSB.append(count);
                selectHTMLItemSB.append("\">");
                selectHTMLItemSB.append("<select class=\"required blockSelectorSelect\"");
                selectHTMLItemSB.append(" id=\"blockSelectorSelect");
                selectHTMLItemSB.append(count);
                selectHTMLItemSB.append("\" class=\"newsletter-forms-input-text\" onchange=\"selectArticlesToTemplate() \">");
                selectHTMLItemSB.append(selectHTMLOptionsSB.toString());
                selectHTMLItemSB.append("</select></div>");
                //String toReplace = templateContent.substring(m.start(), m.end());
                //log.info("Text to replace : " + toReplace);
                //log.info("Text replacement : " + selectHTMLItemSB.toString());
                templateContentTmp = m.replaceFirst(selectHTMLItemSB.toString());//templateContentTmp.replaceFirst(toReplace, selectHTMLItemSB.toString());
                m = patternBlock.matcher(templateContentTmp);
                
                count++;
            }

            StringBuilder resultSB = new StringBuilder("<div class=\"templateArticleSelectorEditor\">");
            resultSB.append(templateContentTmp);
            resultSB.append("</div>");
            result = resultSB.toString();
        } else {
            log.error("Invalid Template Format");
        }
        
        return result;
    }

    /**
     * Validate the template format
     * @param templateContent
     * @return 
     */
    public static String validateTemplateFormat(String templateContent) {
        String result = null;
        String fTagBlockOpen = fixTagsToRegex(TEMPLATE_TAG_BLOCK_OPEN);
        String fTagBlockClose = fixTagsToRegex(TEMPLATE_TAG_BLOCK_CLOSE);
        String fTagBlockTitle = fixTagsToRegex(TEMPLATE_TAG_TITLE);
        String fTagBlockContent = fixTagsToRegex(TEMPLATE_TAG_CONTENT);

        templateContent = templateContent.replace(TEMPLATE_TAG_BLOCK_OPEN, fTagBlockOpen).replace(TEMPLATE_TAG_BLOCK_CLOSE, fTagBlockClose).replace(TEMPLATE_TAG_TITLE, fTagBlockTitle).replace(TEMPLATE_TAG_CONTENT, fTagBlockContent);

        //If the template contains at least one block with one title or content
        if (templateContent.contains(fTagBlockOpen)
                && templateContent.contains(fTagBlockClose)
                && (templateContent.contains(fTagBlockTitle) || templateContent.contains(fTagBlockContent))) {
            result = templateContent;
        }
        return result;
    }

    /**
     * Fix tags to allow replacements using common regular Expressions
     * @param tag
     * @return 
     */
    public static String fixTagsToRegex(String tag) {
        tag = tag.replace("[", "<");
        tag = tag.replace("]", ">");
        return tag;
    }
}

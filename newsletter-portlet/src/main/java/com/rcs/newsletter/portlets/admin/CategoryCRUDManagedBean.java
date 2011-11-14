package com.rcs.newsletter.portlets.admin;

import com.liferay.portal.kernel.log.Log;
import com.rcs.newsletter.core.model.NewsletterCategory;
import com.rcs.newsletter.core.service.NewsletterCategoryService;
import com.rcs.newsletter.core.service.common.ServiceActionResult;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.context.annotation.Scope;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Ariel Parra <ariel@rotterdam-cs.com>
 */
@Named
@Scope("request")
public class CategoryCRUDManagedBean extends NewsletterCRUDManagedBean {

    private static Log log = LogFactoryUtil.getLog(CategoryCRUDManagedBean.class);
    
    @Inject
    NewsletterCategoryService categoryCRUDService;
    
    @Inject
    private UserUiStateManagedBean uiState;
    
    private String name;
    private String fromName;
    private String fromEmail;
    private String description;
    private boolean active;    

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String redirectCategoryList() {
        return "admin?faces-redirect=true";
    }

    public String redirectCreateCategory() {
        uiState.setAdminActiveTabIndex(UserUiStateManagedBean.LISTS_TAB_INDEX);
        this.setAction(CRUDActionEnum.CREATE);
        return "editCategory";
    }

    public String redirectEditCategory() {
        uiState.setAdminActiveTabIndex(UserUiStateManagedBean.LISTS_TAB_INDEX);
        ServiceActionResult serviceActionResult = categoryCRUDService.findById(getId());
        if (serviceActionResult.isSuccess()) {
            NewsletterCategory newsletterCategory = (NewsletterCategory) serviceActionResult.getPayload();
            this.name = newsletterCategory.getName();
            this.description = newsletterCategory.getDescription();
            this.fromEmail = newsletterCategory.getFromEmail();
            this.fromName = newsletterCategory.getFromName();
            this.setAction(CRUDActionEnum.UPDATE);
        }

        return "editCategory";
    }

    public String redirectDeleteCategory() {
        uiState.setAdminActiveTabIndex(UserUiStateManagedBean.LISTS_TAB_INDEX);
        return "deleteCategory";
    }

    public String save() {
        NewsletterCategory newsletterCategory = null;
        String message = "";
        FacesMessage.Severity messageSeverity = null;
        if (getId() == 0) {
            newsletterCategory = new NewsletterCategory();
            fillNewsletterCategory(newsletterCategory);
            ServiceActionResult<NewsletterCategory> saveResult = categoryCRUDService.save(newsletterCategory);

            if (saveResult.isSuccess()) {
                messageSeverity = FacesMessage.SEVERITY_INFO;
            } else {
                messageSeverity = FacesMessage.SEVERITY_ERROR;
            }

        } else {
            ServiceActionResult serviceActionResult = categoryCRUDService.findById(getId());
            if (serviceActionResult.isSuccess()) {
                newsletterCategory = (NewsletterCategory) serviceActionResult.getPayload();
                fillNewsletterCategory(newsletterCategory);

                ServiceActionResult<NewsletterCategory> updateResult = categoryCRUDService.update(newsletterCategory);

                if (updateResult.isSuccess()) {
                    messageSeverity = FacesMessage.SEVERITY_INFO;
                } else {
                    messageSeverity = FacesMessage.SEVERITY_ERROR;
                }
            }
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageSeverity, message, message));

        return redirectCategoryList();
    }

    private void fillNewsletterCategory(NewsletterCategory newsletterCategory) {
        newsletterCategory.setName(name);
        newsletterCategory.setDescription(description);
        newsletterCategory.setFromName(fromName);
        newsletterCategory.setFromEmail(fromEmail);
    }

    public String delete() {
        ServiceActionResult serviceActionResult = categoryCRUDService.findById(getId());
        FacesMessage.Severity messageSeverity = null;
        String message = "";
        if (serviceActionResult.isSuccess()) {
            NewsletterCategory newsletterCategory = (NewsletterCategory) serviceActionResult.getPayload();
            serviceActionResult = categoryCRUDService.delete(newsletterCategory);
        }

        if (serviceActionResult.isSuccess()) {
            messageSeverity = FacesMessage.SEVERITY_INFO;
        } else {
            messageSeverity = FacesMessage.SEVERITY_ERROR;
        }

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageSeverity, message, message));

        return redirectCategoryList();
    }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rcs.newsletter.portlets.onlineviewer;

import com.liferay.portal.util.PortalUtil;
import com.rcs.newsletter.commons.GenericController;
import com.rcs.newsletter.commons.Utils;
import com.rcs.newsletter.core.dto.NewsletterOnlineViewDTO;
import com.rcs.newsletter.core.service.NewsletterArchiveService;
import com.rcs.newsletter.core.service.common.ServiceActionResult;
import java.util.ResourceBundle;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

/**
 *
 * @author marcoslacoste
 */
@Controller
@RequestMapping("VIEW")
public class NewsletterOnlineViewerController extends GenericController {

    private Log logger = LogFactoryUtil.getLog(NewsletterOnlineViewerController.class);
    @Autowired
    private NewsletterArchiveService archiveService;

    @RenderMapping
    public ModelAndView defaultView(RenderRequest request, RenderResponse response) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);

        ModelAndView mav = new ModelAndView("viewer/onlineviewer");
        HttpServletRequest originalRequest = PortalUtil.getOriginalServletRequest(PortalUtil.getHttpServletRequest(request));


        // if the parameters are not received then skip logic
        if (originalRequest.getParameter("nlid") == null || originalRequest.getParameter("sid") == null) {
            return mav;
        }
        
        Long archiveId = Long.valueOf(originalRequest.getParameter("nlid"));
        Long subscriptionId = Long.valueOf(originalRequest.getParameter("sid"));

        // get newsletter content from archive
        ServiceActionResult<NewsletterOnlineViewDTO> result = archiveService.getNewsletterForViewer(archiveId, subscriptionId, Utils.getThemeDisplay(request));

        if (result.isSuccess()) {
            mav.addObject("newsletter", result.getPayload());
        } else {
            mav.addObject("result", result);
        }
        return mav;

    }
}

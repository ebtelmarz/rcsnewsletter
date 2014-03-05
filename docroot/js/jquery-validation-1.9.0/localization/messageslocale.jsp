<%@page import="com.liferay.portal.kernel.util.LocaleUtil"%>
<%@page import="java.util.Locale"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%    
    String languageId = request.getParameter("languageId");
        
    if(languageId==null || languageId== "") {
          languageId = "en_GB";
    }
    Locale locale = LocaleUtil.fromLanguageId(languageId);
    String language = locale.getLanguage();
    String extLocaleFile = "messages_" + language + ".js";
%>

<%@ page contentType="text/javascript" %>

<c:import url="<%=extLocaleFile%>" />
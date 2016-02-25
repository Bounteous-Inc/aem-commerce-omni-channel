<%@page session="false"%><%--
  Copyright 1997-2010 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Carousel component

--%><%@include file="/libs/foundation/global.jsp"%><%
%><%@ page import="com.day.cq.commons.Doctype,
                   com.day.cq.wcm.api.WCMMode,
                   com.day.cq.wcm.api.components.DropTarget,
                   com.day.cq.wcm.foundation.Image,
                   com.day.cq.wcm.foundation.List,
                   com.day.cq.wcm.foundation.Placeholder,
                   com.day.text.Text,
                   java.util.Iterator,
                   com.day.cq.wcm.api.PageFilter,
                   java.util.LinkedHashMap, java.util.Map"
        %><%@include file="/libs/foundation/global.jsp"%><%
%><%
    //List list = (List)request.getAttribute("list");
    List list = new List(slingRequest, new PageFilter());
    if (!list.isEmpty()) {
        // config options
        int playDelay = properties.get("playSpeed", 6000);
        int transTime = properties.get("transTime", 1000);

        // todo: make default designeable
        String controlsType = properties.get("controlsType", "bc");
        boolean showControls = "pn".equals(controlsType);
        if (showControls) {
            controlsType = "";
        } else {
            controlsType = "-" + controlsType;
        }

        // first shove all slides into a map in order to calculate distinct ids
        Map<String, Slide> slides = new LinkedHashMap<String, Slide>();
        Iterator<Page> items = list.getPages();
        String pfx = "cqc-" + Text.getName(resource.getPath()) + "-";
        while (items.hasNext()) {
            Slide slide = new Slide(items.next());
            String name = pfx + slide.name;
            int idx = 0;
            while (slides.containsKey(name)) {
                name = pfx + slide.name + (idx++);
            }
            slide.name = name;
            // prepend context path to img
            slide.img = request.getContextPath() + slide.img;
            slides.put(name, slide);
        }
%><%
    boolean isBackNavigation = resource.isResourceType("wetelco-screens/components/page_product");

    final Page parentPage = currentPage.getParent();
    final Page ancestorPage = parentPage != null ? parentPage.getParent() : null;
    final Iterator<Page> siblings = parentPage != null ? parentPage.listChildren() : null;
%><ul class="wr-Navigation-list">
    <c:forEach var="slide" varStatus="loop" items="<%= slides.values() %>">
        <% Slide slide = (Slide) pageContext.getAttribute("slide"); %>
        <li class="wr-Navigation-item">
            <a class="wr-Navigation-link" href="<%= xssAPI.getValidHref(slide.path) %>.html">
                <img class="wr-Navigation-image" src="<%= xssAPI.getValidHref(slide.img) %>.thumb.500.500.png"/>
                <span class="wr-Navigation-label"><%= xssAPI.encodeForHTMLAttr(slide.title) %></span>
            </a>
        </li>
    </c:forEach>
</ul><%
    }
%>
<%!

    /**
     * Container class for slides
     */
    public static final class Slide {
        private final Page page;
        private String img = "";
        private String title = "";
        private String name = "";
        private String desc = "";
        private String path = "";

        private Slide(Page page) {
            this.page = page;
            title = page.getTitle();
            desc = page.getDescription();
            if (desc == null) {
                desc = "";
            }
            path = page.getPath();
            // currently we just check if "image" resource is present
            Resource r = page.getContentResource("image");
            if (r != null) {
                Image image = new Image(r);
                img = page.getPath() + ".img.png" + image.getSuffix();
            }
            name = page.getName();
        }

        public Page getPage() {
            return page;
        }

        public String getImg() {
            return img;
        }

        public String getTitle() {
            return title;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getPath() {
            return path;
        }
    }
%>

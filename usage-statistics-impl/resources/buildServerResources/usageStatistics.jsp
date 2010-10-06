<%--
  ~ Copyright 2000-2010 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ include file="/include.jsp"%>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<jsp:useBean id="statisticsData" scope="request" type="jetbrains.buildServer.controllers.UsageStatisticsBean"/>

<bs:linkScript>
  ${teamcityPluginResourcesPath}js/usageStatistics.js
</bs:linkScript>
<bs:linkCSS>
  ${teamcityPluginResourcesPath}css/usageStatistics.css
  /css/main.css
  /css/profilePage.css
  /css/admin/serverConfig.css
</bs:linkCSS>

<div>
  <bs:refreshable containerId="usageStatisticsReportingStatusMessageContainer" pageUrl="${pageUrl}">
    <bs:messages key="usageStatisticsReportingStatusMessage"/>
  </bs:refreshable>
</div>
<c:if test="${empty param['updateMessages']}">
  <div>
    <div>Please allow us know a bit more about your TeamCity usage. We are not watching you and not collecting any user/project sensitive data, just numbers. Help us improve the tool!</div>
    <div>
      <input type="checkbox"
             id="reportingEnabledCheckbox"
             onclick="BS.UsageStatistics.updateReportingStatus();"
             <c:if test="${statisticsData.reportingEnabled}">checked</c:if>
      >
      <label for="reportingEnabledCheckbox" class="checkBoxLabel">Periodically send this statistics to JetBrains</label>
      <forms:saving id="usageStatisticsReportingStatusUpdatingProgress" style="float:left;"/>
      &nbsp;
    </div>
    <div style="height: 10px;"></div>
  </div>
  <bs:refreshable containerId="usageStatisticsStatus" pageUrl="${pageUrl}">
    <span style="float: left; padding-top: 1px; padding-bottom: 1px;"
      ><c:if test="${statisticsData.collectingNow}"><img src="<c:url value='/img/buildStates/running_green_transparent.gif'/>" class="icon"/> </c:if
      >Usage statistics data was <c:choose
        ><c:when test="${statisticsData.statisticsCollected}">collected <bs:date smart="true" no_smart_title="true" value="${statisticsData.lastCollectingFinishDate}"/></c:when
        ><c:otherwise>not collected yet</c:otherwise
      ></c:choose><c:choose
        ><c:when test="${statisticsData.collectingNow}"> and is being collected now...</span></c:when
        ><c:otherwise>.</span> <input type="button" value="Collect Now" class="collectNowButton" onclick="BS.UsageStatistics.forceCollectingNow();"><forms:saving id="usageStatisticsCollectNowProgress" style="float: left;"/></c:otherwise
      ></c:choose>
    <c:if test="${statisticsData.statisticsCollected}">
      <div class="downloadLink">
        <a class="downloadLink" href="<c:url value="/admin/downloadUsageStatistics.html"/>">Download (~${statisticsData.sizeEstimate})</a>
      </div>
    </c:if>
    <script type="text/javascript">
      <c:choose>
        <c:when test="${statisticsData.statisticsCollected}">BS.UsageStatistics.onStatusUpdated(${statisticsData.lastCollectingFinishDate.time});</c:when>
        <c:otherwise>BS.UsageStatistics.onStatusUpdated(-1);</c:otherwise>
      </c:choose>
    </script>
  </bs:refreshable>
  <bs:refreshable containerId="usageStatisticsContent" pageUrl="${pageUrl}">
    <c:if test="${statisticsData.statisticsCollected}">
      <br/><br/>
      <div>
        <c:set var="groups" value="${statisticsData.statisticGroups}"/>
        <c:choose>
          <c:when test="${empty groups}">
            <span>No statistics data was published.</span>
          </c:when>
          <c:otherwise>
            <c:forEach var="group" items="${groups}" varStatus="status">
              <div class="statisticGroup" id="group-${status.index}">
                <l:settingsBlock title="${group.key}">
                  <div class="statisticGroupInner">
                    <c:set var="statisticsGroup" value="${group.value}" scope="request"/>
                    <jsp:include page="${group.value.jspPagePath}"/>
                  </div>
                </l:settingsBlock>
              </div>
            </c:forEach>
            <script type="text/javascript">
              BS.UsageStatistics.sortGroups(${fn:length(groups)});
            </script>
          </c:otherwise>
        </c:choose>
      </div>
    </c:if>
  </bs:refreshable>
  <script type="text/javascript">
    BS.UsageStatistics.scheduleStatusUpdating();
  </script>
</c:if>

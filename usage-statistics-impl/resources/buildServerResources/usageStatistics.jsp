<%--
  ~ Copyright 2000-2014 JetBrains s.r.o.
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

<div>
  <bs:refreshable containerId="usageStatisticsReportingStatusMessageContainer" pageUrl="${pageUrl}">
    <bs:messages key="usageStatisticsReportingStatusMessage"/>
  </bs:refreshable>
</div>
<c:if test="${empty param['updateMessages']}">
  <authz:authorize allPermissions="CHANGE_SERVER_SETTINGS">
    <div>
      <div style="margin-bottom: 1em">Please let us learn a bit more about your TeamCity usage. We are not watching you and not collecting any user- or project-sensitive data, just numbers. Help us improve the tool!</div>
      <bs:refreshable containerId="usageStatisticsReportingCheckboxContainer" pageUrl="${pageUrl}">
        <forms:checkbox
          name=""
          id="reportingEnabledCheckbox"
          onclick="BS.UsageStatistics.updateReportingStatus();"
          checked="${statisticsData.reportingEnabled}"/>
        <label for="reportingEnabledCheckbox">Periodically send this statistics to JetBrains</label>
        <forms:saving id="usageStatisticsReportingStatusUpdatingProgress" className="progressRingInline"/>
      </bs:refreshable>
    </div>
  </authz:authorize>
  <bs:refreshable containerId="usageStatisticsStatus" pageUrl="${pageUrl}">
    <div style="margin-top: 1em">
      <c:if test="${statisticsData.collectingNow}"> </c:if
        >Usage statistics data was <c:choose
          ><c:when test="${statisticsData.statisticsCollected}">collected <bs:date smart="true" no_smart_title="true" value="${statisticsData.lastCollectingFinishDate}"/></c:when
          ><c:otherwise>not collected yet</c:otherwise
        ></c:choose><c:choose
          ><c:when test="${statisticsData.collectingNow}"> and is being collected now...<forms:progressRing className="progressRingInline"/></c:when
          ><c:otherwise>. <input type="button" value="Collect Now" class="btn btn_mini" onclick="BS.UsageStatistics.forceCollectingNow();"><forms:saving id="usageStatisticsCollectNowProgress" className="progressRingInline"/></c:otherwise
        ></c:choose>
      <c:if test="${statisticsData.statisticsCollected}">
        <div class="downloadLink">
          <a class="downloadLink tc-icon_before icon16 tc-icon_download" href="<c:url value="/admin/downloadUsageStatistics.html"/>">Download (~${statisticsData.sizeEstimate})</a>
        </div>
      </c:if>
      <script type="text/javascript">
        <c:choose>
          <c:when test="${statisticsData.statisticsCollected}">BS.UsageStatistics.onStatusUpdated(${statisticsData.lastCollectingFinishDate.time});</c:when>
          <c:otherwise>BS.UsageStatistics.onStatusUpdated(-1);</c:otherwise>
        </c:choose>
      </script>
    </div>
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
                    <c:set var="statisticsGroup" value="${group.value.second}" scope="request"/>
                    <jsp:include page="${group.value.first}"/>
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

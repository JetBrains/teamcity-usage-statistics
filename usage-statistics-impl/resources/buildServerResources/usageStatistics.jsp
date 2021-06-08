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
<%--@elvariable id="editAllowed" type="java.lang.Boolean"--%>

<div>
  <bs:refreshable containerId="usageStatisticsReportingStatusMessageContainer" pageUrl="${pageUrl}">
    <bs:messages key="usageStatisticsReportingStatusMessage"/>
  </bs:refreshable>
</div>
<c:if test="${empty param['updateMessages']}">
  <c:if test="${editAllowed}">
    <div>
      <bs:refreshable containerId="usageStatisticsReportingCheckboxContainer" pageUrl="${pageUrl}">
        <forms:checkbox
            name=""
            id="reportingEnabledCheckbox"
            onclick="BS.UsageStatistics.updateReportingStatus();"
            checked="${statisticsData.reportingEnabled}"/>
        <label for="reportingEnabledCheckbox">Periodically send usage statistics to JetBrains</label>
        <forms:saving id="usageStatisticsReportingStatusUpdatingProgress" className="progressRingInline"/>
      </bs:refreshable>
    </div>
  </c:if>

  <div>
    <div class="grayNote" style="margin-bottom: 0.5em; margin-left: 1.8em;">Help us improve TeamCity by sending anonymous data about your usage statistics.<br>
      We do not collect any private or personal data related to users or source projects. The statistics is gathered anonymously and used by the TeamCity team only for analyzing and prioritizing the needs of our users when working on the future versions of TeamCity. The collected data complies with the <a href="https://www.jetbrains.com/legal/docs/privacy/privacy.html">JetBrains Privacy Policy</a>.<br>
    </div>

    <hr/>

    <div style="margin-bottom: 1em">You can preview what data will be collected. Note that this operation might take up to several minutes. The task will be performed in the background, so you can safely close this page.</div>
    <bs:refreshable containerId="usageStatisticsStatus" pageUrl="${pageUrl}">
        <c:if test="${statisticsData.collectingNow}"> </c:if
        >Usage statistics data was <c:choose
      ><c:when test="${statisticsData.statisticsCollected}">collected <bs:date smart="true" no_smart_title="true" value="${statisticsData.lastCollectingFinishDate}"/></c:when
      ><c:otherwise>not collected yet</c:otherwise
      ></c:choose><c:choose
      ><c:when test="${statisticsData.collectingNow}"> and is being collected now...<forms:progressRing className="progressRingInline"/></c:when
      ><c:otherwise>. <input type="button" value="Collect statistics now" class="btn btn_mini" onclick="BS.UsageStatistics.forceCollectingNow();"><forms:saving id="usageStatisticsCollectNowProgress" className="progressRingInline"/></c:otherwise
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
    </bs:refreshable>
  </div>

  <div class="clearfix">
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
  </div>
  <script type="text/javascript">
    BS.UsageStatistics.scheduleStatusUpdating();
  </script>
</c:if>

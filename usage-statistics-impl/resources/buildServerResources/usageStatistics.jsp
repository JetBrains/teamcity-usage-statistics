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
  </div>
  <bs:refreshable containerId="usageStatisticsStatus" pageUrl="${pageUrl}">
    <div style="height: 10px;"></div>
    <span style="float: left; padding-top: 1px; padding-bottom: 1px;"
      ><c:if test="${statisticsData.collectingNow}"><img src="<c:url value='/img/buildStates/running_green_transparent.gif'/>" class="icon"/> </c:if
      >Usage statistics were <c:choose
        ><c:when test="${statisticsData.statisticsCollected}">collected <bs:date smart="true" value="${statisticsData.lastCollectingFinishDate}"/></c:when
        ><c:otherwise>not collected yet</c:otherwise
      ></c:choose><c:choose
        ><c:when test="${statisticsData.collectingNow}"> and are being collected now...</span></c:when
        ><c:otherwise>.</span> <input type="button" value="Collect Now" class="collectNowButton" onclick="BS.UsageStatistics.forceCollectingNow();"><forms:saving id="usageStatisticsCollectNowProgress" style="float: left;"/></c:otherwise
      ></c:choose>
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
        <c:set var="statistics" value="${statisticsData.statistics}"/>
        <c:choose>
          <c:when test="${empty statistics}">
            <span>No statistics were published.</span>
          </c:when>
          <c:otherwise>
            <c:forEach var="group" items="${statistics}" varStatus="status">
              <div class="statisticGroup" id="group-${status.index}">
                <l:settingsBlock title="${group.key}" style="">
                  <table style="width: 100%" cellspacing="0">
                    <c:forEach var="statistic" items="${group.value}">
                      <tr class="highlightRow statisticRow">
                        <td><c:out value="${statistic.displayName}"/></td>
                        <td style="width: 13%"><c:out value="${statistic.formattedValue}"/></td>
                      </tr>
                    </c:forEach>
                  </table>
                </l:settingsBlock>
              </div>
            </c:forEach>
            <script type="text/javascript">
              BS.UsageStatistics.sortGroups(${fn:length(statistics)});
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

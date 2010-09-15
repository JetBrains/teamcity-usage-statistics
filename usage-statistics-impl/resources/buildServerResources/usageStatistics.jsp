<%@ include file="/include.jsp"%>
<jsp:useBean id="statisticsData" scope="request" type="jetbrains.buildServer.controllers.UsageStatisticsBean"/>

<bs:linkScript>
  ${teamcityPluginResourcesPath}js/usageStatistics.js
</bs:linkScript>
<bs:linkCSS>
  ${teamcityPluginResourcesPath}css/usageStatistics.css
</bs:linkCSS>

<div style="width: 70%; height: 2em;">
  <bs:refreshable containerId="usageStatisticsReportingStatusMessageContainer" pageUrl="${pageUrl}">
    <bs:messages key="usageStatisticsReportingStatusMessage"/>
  </bs:refreshable>
</div>
<c:if test="${empty param['updateMessages']}">
  <div>
    <input type="checkbox"
           id="reportingEnabledCheckbox"
           class="left"
           onclick="BS.UsageStatistics.updateReportingStatus();"
           <c:if test="${statisticsData.reportingEnabled}">checked</c:if>
    >
    <label for="reportingEnabledCheckbox" class="checkBoxLabel">Report following statistics to JetBrains periodically</label>
    <forms:saving id="usageStatisticsReportingStatusUpdatingProgress" style="float:left;"/>
    &nbsp;
  </div>
  <br/>
  <div>
    <c:set var="statistics" value="${statisticsData.statistics}"/>
    <c:choose>
      <c:when test="${empty statistics}">
        <span>No statistics were published.</span>
      </c:when>
      <c:otherwise>
        <table class="usageStatisticsTable">
          <tr>
            <th>Statistic</th>
            <th>Value</th>
          </tr>
          <c:forEach var="statistic" items="${statistics}">
            <tr>
              <td><c:out value="${statistic.displayName}"/></td>
              <td class="text-center"><c:out value="${statistic.formattedValue}"/></td>
            </tr>
          </c:forEach>
        </table>
      </c:otherwise>
    </c:choose>
  </div>
</c:if>

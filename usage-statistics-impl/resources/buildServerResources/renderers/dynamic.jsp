

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><jsp:useBean id="statisticsGroup" scope="request" type="jetbrains.buildServer.usageStatistics.presentation.renderers.DynamicUsageStatisticsGroup"
/><table class="borderBottom" style="width: 99%;" cellspacing="0">
  <tr class="statisticRow">
    <th>&nbsp;</th>
    <c:forEach var="period" items="${statisticsGroup.periods}">
      <th style="text-align:left;"><c:out value="${period}"/></th>
    </c:forEach>
  </tr>
  <c:forEach var="statistic" items="${statisticsGroup.statistics}" varStatus="statisticIndex">
    <tr class="statisticRow<c:if test="${statisticIndex.last}"> noBorder</c:if>">
      <td><c:out value="${statistic.displayName}"/></td>
      <c:forEach var="valueInfo" items="${statistic.valueInfos}">
        <c:set var="tooltip" value="${valueInfo.tooltip}"/>
        <td style="width: 13%; white-space: nowrap;" <c:if test="${not empty tooltip}">title="${tooltip}"</c:if>><c:out value="${valueInfo.value}"/></td>
      </c:forEach>
    </tr>
  </c:forEach>
</table>
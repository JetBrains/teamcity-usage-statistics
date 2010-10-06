<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><jsp:useBean id="statisticsGroup" scope="request" type="jetbrains.buildServer.usageStatistics.presentation.renderers.DefaultUsageStatisticsGroup"
/><table style="width: 99%;" cellspacing="0">
  <c:forEach var="statistic" items="${statisticsGroup.statistics}">
    <tr class="highlightRow statisticRow">
      <td><c:out value="${statistic.displayName}"/></td>
      <td style="width: 13%"><c:out value="${statistic.formattedValue}"/></td>
    </tr>
  </c:forEach>
</table>


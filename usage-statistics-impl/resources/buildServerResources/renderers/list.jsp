

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><jsp:useBean id="statisticsGroup" scope="request" type="jetbrains.buildServer.usageStatistics.presentation.renderers.SimpleUsageStatisticsGroup"
/><table class="borderBottom" style="width: 99%;" cellspacing="0">
  <c:forEach var="statistic" items="${statisticsGroup.statistics}" varStatus="statisticIndex">
    <tr class="statisticRow<c:if test="${statisticIndex.last}"> noBorder</c:if>">
      <td><c:out value="${statistic.displayName}"/></td>
    </tr>
  </c:forEach>
</table>
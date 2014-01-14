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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><jsp:useBean id="statisticsGroup" scope="request" type="jetbrains.buildServer.usageStatistics.presentation.renderers.SimpleUsageStatisticsGroup"
/><table class="borderBottom" style="width: 99%;" cellspacing="0">
  <c:forEach var="statistic" items="${statisticsGroup.statistics}" varStatus="statisticIndex">
    <tr class="statisticRow<c:if test="${statisticIndex.last}"> noBorder</c:if>">
      <td><c:out value="${statistic.displayName}"/></td>
      <c:set var="tooltip" value="${statistic.valueTooltip}"/>
      <td style="width: 13%; white-space: nowrap;" <c:if test="${not empty tooltip}">title="${tooltip}"</c:if>><c:out value="${statistic.formattedValue}"/></td>
    </tr>
  </c:forEach>
</table>


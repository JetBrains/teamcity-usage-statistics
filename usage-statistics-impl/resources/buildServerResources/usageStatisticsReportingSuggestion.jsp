<%--
  ~ Copyright 2000-2011 JetBrains s.r.o.
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
<jsp:useBean id="showSuggestion" scope="request" type="java.lang.Boolean"/>
<script type="text/javascript">
  BS.UsageStatisticsReportingSuggestion = {
    makeDecision: function(decision) {
      BS.Util.show('usageStatisticsReportingSuggestionProgress');
      BS.ajaxRequest(window['base_uri'] + "/admin/usageStatistics.html", {
        method: "post",
        parameters: "reportingEnabled=" + decision,
        onComplete: function(transport) {
          $('usageStatisticsReportingSuggestionContainer').refresh('usageStatisticsReportingSuggestionProgress');
        }
      });
    }
  };
</script>
<bs:refreshable containerId="usageStatisticsReportingSuggestionContainer" pageUrl="${pageUrl}">
  <bs:messages key="usageStatisticsReportingStatusMessage" style="margin-bottom: 1em;"/>
  <c:if test="${showSuggestion}">
    <div class="attentionComment" style="margin-bottom: 1em;">
      Would you like to send anonymous <a href="<c:url value="/admin/admin.html?item=usageStatistics"/>">usage statistics</a> to TeamCity development team (can be turned off at any time)?
      <input type="button" class="btn btn_mini btn_primary" value="Yes, I would!" onclick="BS.UsageStatisticsReportingSuggestion.makeDecision(true);">
      <input type="button" class="btn btn_mini" value="No, I would not." onclick="BS.UsageStatisticsReportingSuggestion.makeDecision(false);">
      <forms:progressRing id="usageStatisticsReportingSuggestionProgress" style="display: none;"/>
    </div>
  </c:if>
</bs:refreshable>
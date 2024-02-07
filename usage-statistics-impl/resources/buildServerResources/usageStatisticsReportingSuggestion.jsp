
<%@ include file="/include.jsp"%>
<jsp:useBean id="showSuggestion" scope="request" type="java.lang.Boolean"/>
<bs:refreshable containerId="usageStatisticsReportingSuggestionContainer" pageUrl="${pageUrl}">
  <bs:messages key="usageStatisticsReportingStatusMessage" style="margin-bottom: 1em;"/>
  <c:if test="${showSuggestion}">
    <script type="text/javascript">
      BS.UsageStatisticsReportingSuggestion = {
        makeDecision: function(decision) {
          BS.ajaxRequest(window['base_uri'] + "/admin/usageStatistics.html", {
            method: "post",
            parameters: "reportingEnabled=" + decision,
            onComplete: function(transport) {
              $('usageStatisticsReportingSuggestionContainer').refresh();
              if ($('usageStatisticsReportingCheckboxContainer')) { // we are on the usage statistics page
                $('usageStatisticsReportingCheckboxContainer').refresh();
              }
            }
          });
        }
      };
    </script>
    <div class="messagePrompt">
      Would you like to help improve TeamCity by sending anonymous <a href="<c:url value="/admin/admin.html?item=usageStatistics"/>">usage statistics</a> to the TeamCity development team?<br/>This can be turned off at any time under "Usage Statistics".
      <p class="messagePromptButtons">
        <input type="button" class="btn" value="Yes, please" onclick="BS.UsageStatisticsReportingSuggestion.makeDecision(true);">
        <input type="button" class="btn" value="No, thank you" onclick="BS.UsageStatisticsReportingSuggestion.makeDecision(false);">
      </p>
    </div>
  </c:if>
</bs:refreshable>
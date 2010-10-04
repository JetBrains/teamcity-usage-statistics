<jsp:useBean id="webPagesUsageStatisticsProvider" scope="request" type="jetbrains.buildServer.usageStatistics.impl.providers.WebPagesUsageStatisticsProvider"
/><%
  if ("get".equalsIgnoreCase(request.getMethod())) {
    webPagesUsageStatisticsProvider.processGetRequest(request);
  }
%>
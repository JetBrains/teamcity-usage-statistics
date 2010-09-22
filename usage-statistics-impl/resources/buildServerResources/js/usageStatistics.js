/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

BS.UsageStatistics = {
  updateReportingStatus: function() {
    BS.Util.show('usageStatisticsReportingStatusUpdatingProgress');

    BS.ajaxRequest(base_uri + "/admin/usageStatistics.html", {
      method: "post",
      parameters: "reportingEnabled=" + $('reportingEnabledCheckbox').checked,
      onComplete: function(transport) {
        $('usageStatisticsReportingStatusMessageContainer').refresh('usageStatisticsReportingStatusUpdatingProgress', 'updateMessages=true', function() {
          if (transport.responseText.indexOf("error") != -1) {
            $('reportingEnabledCheckbox').checked = !$('reportingEnabledCheckbox').checked;
          }
        });
      }
    });
  },

  sortGroups: function(count) {
    var heights = [];
    for (var k = 0; k < count; k++) {
      heights[k] = $('group-' + k).offsetHeight;
    }

    var leftHeight = 0, rightHeight = 0;
    for (var i = 0; i < count; i++) {
      var group = $('group-' + i);
      if (leftHeight <= rightHeight) {
        group.style['float'] = 'left';
        leftHeight += heights[i];
      }
      else {
        group.style['float'] = 'right';
        rightHeight += heights[i];
      }
    }
  }
};

/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
  _lastCollectingFinishTimestamp: null,

  updateReportingStatus: function() {
    BS.Util.show('usageStatisticsReportingStatusUpdatingProgress');

    BS.ajaxRequest(window['base_uri'] + "/admin/usageStatistics.html", {
      method: "post",
      parameters: "reportingEnabled=" + $('reportingEnabledCheckbox').checked,
      onComplete: function(transport) {
        $('usageStatisticsReportingStatusMessageContainer').refresh('usageStatisticsReportingStatusUpdatingProgress', 'updateMessages=true', function() {
          if (transport.responseText.indexOf("error") == -1) {
            if ($("usageStatisticsReportingSuggestionContainer")) { // usage statistics reporting suggestion is shown
              BS.Util.fadeOutAndDelete("#usageStatisticsReportingSuggestionContainer");
            }
          }
          else {
            $('reportingEnabledCheckbox').checked = !$('reportingEnabledCheckbox').checked;
          }
        });
      }
    });
  },

  forceCollectingNow: function() {
    BS.Util.show('usageStatisticsCollectNowProgress');

    BS.ajaxRequest(window['base_uri'] + "/admin/usageStatistics.html", {
      method: "post",
      parameters: "forceCollectingNow=true",
      onComplete: function() {
        $('usageStatisticsStatus').refresh();
      }
    });
  },

  scheduleStatusUpdating: function() {
    setTimeout(function () {
      BS.UsageStatistics.updateStatus();
    }, 10000);
  },

  updateStatus: function() {
    $('usageStatisticsStatus').refresh();
    BS.UsageStatistics.scheduleStatusUpdating();
  },

  onStatusUpdated: function(lastCollectingFinishTimestamp) {
    if (BS.UsageStatistics._lastCollectingFinishTimestamp != null && BS.UsageStatistics._lastCollectingFinishTimestamp != lastCollectingFinishTimestamp) {
      $('usageStatisticsContent').refresh();
    }

    BS.UsageStatistics._lastCollectingFinishTimestamp = lastCollectingFinishTimestamp;
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
        group.addClassName('statisticGroupLeft');
        leftHeight += heights[i];
      }
      else {
        group.addClassName('statisticGroupRight');
        rightHeight += heights[i];
      }
    }
  }
};

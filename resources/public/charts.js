var chart;
var qs  = window.location.search;

var academic_endyear = function() {
    var now = new Date();
    return new Date(now.getFullYear(), now.getMonth() + 6, now.getDay()).getFullYear();
}();

var default_date_range = {};
var default_series = [];
for (startyear=2010; startyear < academic_endyear; startyear++) {
    var endyear_str = (startyear + 1).toString();
    var startyear_str = startyear.toString();
    var disp_string = startyear_str + "-" + endyear_str;
    default_date_range[disp_string] = {'start': startyear_str + '-07-01', 'end': endyear_str + '-06-30'};
    default_series.push({name: disp_string, data: []});
}

var charts = {
  
  ticketsGroupedByCreator : function(){
    var options = {
      title: { 
        text: 'Tickets grouped by creator'
      },
      chart : {
          renderTo: 'chart',
          type:'pie',
      },
      credits: {
        enabled: false
      },
      series: [{
                  name: 'created', 
                  data: [],
                  cursor: 'pointer',
                  point: {
                     events: {
                          click: function() {
                            var start =  getQueryStringValue("start") || "2010-08-01";
                            var end   =   getQueryStringValue("end") || "2100-01-01";
                            var url   = "/tickets-by-creator.json?name=" + this.name + "&start=" + start + "&end=" + end;
                            destroyTable();
                            $.getJSON(url, function(data){
                               loadTable(data.data, data.columns);
                            });
                          }
                      }
                  }

              }]
      }
    $.getJSON('/tickets-grouped-by-creator.json' + qs, function(response){
      $.each(response.data, function(i, v){
        options.series[0].data.push({y:v.count, name: v.username});
      })
      chart = new Highcharts.Chart(options);
    });
  },
  ticketsGroupedByResolver : function(){
    var options = {
      title: { 
        text: 'Tickets grouped by resolver'
      },
      chart : {
          renderTo: 'chart',
          type:'pie'
      },
      credits: {
        enabled: false
      },
      series: [
        {
            name: 'resolved', 
            data: [],
            cursor: 'pointer',
            point: {
               events: {
                    click: function() {
                      var start =  getQueryStringValue("start") || "2010-08-01";
                      var end   =  getQueryStringValue("end") || "2100-01-01";
                      var url   = "/tickets-by-resolver.json?name=" + this.name + "&start=" + start + "&end=" + end;
                      destroyTable();
                      $.getJSON(url, function(data){
                         loadTable(data.data, data.columns);
                      });
                    }
                }
            }
        }]
      };
    $.getJSON('/tickets-grouped-by-resolver.json' + qs, function(response){
      $.each(response.data, function(i, v){
        options.series[0].data.push({y:v.count, name: v.username});
      })
      chart = new Highcharts.Chart(options);
    });
  },
  openTicketsGroupedByOwner : function(){
    var options = {
      title: { 
        text: 'Open tickets grouped by owner'
      },
      chart : {
          renderTo: 'chart',
          type:'pie'
      },
      credits: {
        enabled: false
      },
      series: [
        {
            name: 'open tickets', 
            data: [],
            cursor: 'pointer',
            point: {
               events: {
                    click: function() {
                      var start =  getQueryStringValue("start") || "2010-08-01";
                      var end   =  getQueryStringValue("end") || "2100-01-01";
                      var url   = "/open-tickets-by-owner.json?name=" + this.name + "&start=" + start + "&end=" + end;
                      destroyTable();
                      $.getJSON(url, function(data){
                         loadTable(data.data, data.columns);
                      });
                    }
                }
            }
        }]
      };
    $.getJSON('/open-tickets-grouped-by-owner.json' + qs, function(response){
      $.each(response.data, function(i, v){
        options.series[0].data.push({y:v.count, name: v.username});
      })
      chart = new Highcharts.Chart(options);
    });
  },
  ticketsGroupedByExternalUserWaitWeek : function(){
    var nameFormatter = function (name){
      return name + ' week' + (name === 1 ? '' : 's');
    };
    var options = {
      title: { 
        text: 'Tickets grouped by external user wait week'
      },
      chart : {
          renderTo: 'chart',
          type:'pie'
      },
      credits: {
        enabled: false
      },
      series: [
        {
            name: 'open tickets', 
            data: [],
            cursor: 'pointer',
            point: {
               events: {
                    click: function() {
                      var start =  getQueryStringValue("start") || "2000-01-01";
                      var end   =  getQueryStringValue("end") || "3000-01-01";
                      var url   = "/tickets-by-external-user-wait-week.json?week=" + this.week + "&start=" + start + "&end=" + end;
                      destroyTable();
                      $.getJSON(url, function(data){
                         loadTable(data.data, data.columns);
                      });
                    }
                }
            },
            tooltip: {
              pointFormat: '<b>{point.percentage:.0f}% ( {point.y} tickets )</b><br/>'
            }
        }]
      };
    $.getJSON('/tickets-grouped-by-external-user-wait-week.json' + qs, function(response){
      $.each(response.data, function(i, v){
        options.series[0].data.push({y:v.count, name: nameFormatter(v.week), week: v.week});
      })
      chart = new Highcharts.Chart(options);
    });
  },
  ticketsGroupedByInternalUserWaitWeek : function(){
    var nameFormatter = function (name){
      return name + ' week' + (name === 1 ? '' : 's');
    };
    var options = {
      title: { 
        text: 'Tickets grouped by internal user wait week'
      },
      chart : {
          renderTo: 'chart',
          type:'pie'
      },
      credits: {
        enabled: false
      },
      series: [
        {
            name: 'open tickets', 
            data: [],
            cursor: 'pointer',
            point: {
               events: {
                    click: function() {
                      var start =  getQueryStringValue("start") || "2000-01-01";
                      var end   =  getQueryStringValue("end") || "3000-01-01";
                      var url   =  "/tickets-by-internal-user-wait-week.json?week=" + this.week + "&start=" + start + "&end=" + end;
                      destroyTable();
                      $.getJSON(url, function(data){
                         loadTable(data.data, data.columns);
                      });
                  }
                }
            },
            tooltip: {
              pointFormat: '<b>{point.percentage:.0f}% ( {point.y} tickets )</b><br/>'
            }
        }]
      };
    $.getJSON('/tickets-grouped-by-internal-user-wait-week.json' + qs, function(response){
      $.each(response.data, function(i, v){
        options.series[0].data.push({y:v.count, name: nameFormatter(v.week), week: v.week});
      })
      chart = new Highcharts.Chart(options);
    });
  },

  ticketsGroupedByMonthCreated : function(){
    var options = {
      title: { 
        text: 'Tickets grouped by month created'
      },
      chart : {
          renderTo: 'chart',
      },
      credits: {
        enabled: false
      },
      xAxis : {
        categories : ['Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']
      },
      yAxis: {
        title: { 
          text : "Tickets created"
        }
      },
      tooltip: {
        shared: true,
        crosshairs: true
      },
      
      series: JSON.parse(JSON.stringify(default_series)),
            
      };
      var date_range = JSON.parse(JSON.stringify(default_date_range));
      chart = new Highcharts.Chart(options); 
      $.each(date_range, function (label, dateParams){
        $.getJSON('/tickets-grouped-by-month-created.json', dateParams, function(response){
          var seriesArr = [];
          $.each(response.data, function(i, v){
            seriesArr.push(v.count);
          });
          $.each (chart.series, function (i, seriesData){
            if (seriesData.name == label){
              seriesData.setData(seriesArr, true);
            }
          })
        });
      });    
  },
  averageOpenTicketDaysGroupedByMonthCreated : function(){
    var options = {
      title: { 
        text: 'Average open ticket days grouped by month created'
      },
      chart : {
          renderTo: 'chart',
      },
      credits: {
        enabled: false
      },
      xAxis : {
        categories : ['Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']
      },
      yAxis: {
        title: { 
          text : "Average open ticket days"
        }
      },
      tooltip: {
        shared: true,
        crosshairs: true
      },
      
      legend: {
        layout: 'vertical',
        labelFormatter: function(){
          var label = this.name;
          if (this.avgdays){
            label += ' ( ' + this.avgdays + ' average open days )';
          }
          return label;
          
        }
      },
      
      series: JSON.parse(JSON.stringify(default_series)),
      };
      var date_range = JSON.parse(JSON.stringify(default_date_range));
      chart = new Highcharts.Chart(options); 
      $.each(date_range, function (label, dateParams){
        $.getJSON('/average-open-ticket-days-grouped-by-month-created.json', dateParams, function(response){
          var seriesArr = [];
          $.each(response.data.months, function(i, v){
            seriesArr.push(v.avgdays);
          });
          
          $.each (chart.series, function (i, seriesData){
            if (seriesData.name == label){
              seriesData.setData(seriesArr, false);
              seriesData.avgdays = response.data.overall[0].avgdays;
              chart.series[i].legendItem = chart.series[i].legendItem.destroy();
              chart.isDirtyLegend = true;
              chart.redraw();
            }
          });
        });
      });    
  },
  averageFirstResponseWaitDaysGroupedByMonthCreated : function(){
    var options = {
      title: { 
        text: 'Average first response wait days grouped by month created'
      },
      chart : {
          renderTo: 'chart',
      },
      credits: {
        enabled: false
      },
      xAxis : {
        categories : ['Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']
      },
      yAxis: {
        title: { 
          text : "Average first response wait days"
        }
      },
      tooltip: {
        shared: true,
        crosshairs: true
      },
      
      legend: {
        layout: 'vertical',
        labelFormatter: function(){
          var label = this.name;
          if (this.avgdays){
            label += ' ( ' + this.avgdays + ' average first response wait days )';
          }
          return label;
          
        }
      },
      
      series: JSON.parse(JSON.stringify(default_series)),
      
      };
      var date_range = JSON.parse(JSON.stringify(default_date_range));
      chart = new Highcharts.Chart(options); 
      $.each(date_range, function (label, dateParams){
        $.getJSON('/average-first-response-wait-days-grouped-by-month-created.json', dateParams, function(response){
          var seriesArr = [];
          $.each(response.data.months, function(i, v){
            seriesArr.push(v.avgdays);
          });
          
          $.each (chart.series, function (i, seriesData){
            if (seriesData.name == label){
              seriesData.setData(seriesArr, false);
              seriesData.avgdays = response.data.overall[0].avgdays;
              chart.series[i].legendItem = chart.series[i].legendItem.destroy();
              chart.isDirtyLegend = true;
              chart.redraw();
            }
          });
        });
      });    
  },
  ticketsOpenedAddedAndResolvedByMonth : function(){
    var options = {
      title: { 
        text: 'Tickets opened, added, and resolved by month'
      },
      chart : {
          renderTo: 'chart',
      },
      credits: {
        enabled: false
      },
      xAxis : {
        categories : ['Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']
      },
      yAxis: {
        title: { 
          text : "Tickets"
        }
      },
      tooltip: {
        shared: true,
        crosshairs: true
      },
      
      series: JSON.parse(JSON.stringify(default_series)),
      
      };
      var date_range = JSON.parse(JSON.stringify(default_date_range));
      chart = new Highcharts.Chart(options); 
      $.each(date_range, function (label, dateParams){
        $.getJSON('/open-tickets-at-start-of-months.json', dateParams, function(response){
          var seriesArr = [];
          $.each(response.data, function(i, v){
            seriesArr.push(v.count);
          });
          
          $.each (chart.series, function (i, seriesData){
            if (seriesData.name == label){
              seriesData.setData(seriesArr, true);
            }
          });
        });
      });    
  }
  
  
  
};


  
function loadChartByHash(){
  var name = location.hash.substring(1);
  if (name in charts){
    destroyExistingChart();
    destroyTable();
    if (name.search("Month") >= 0){
      $('#options').hide();
    } else {
      $('#options').show();
    }
    if (name.search("Wait") >=0){
      $('#top').prop('disabled', true).parent('li').hide();
    } else {
      $('#top').prop('disabled', false).parent('li').show();
    }
    charts[name]();
  }
}

function destroyExistingChart(){
  if (chart){
    chart.destroy();
  }
}

function getQueryStringValue( key ) {
  var queryStringArray = location.search.substr( 1 ).split( '&' );
  for ( var i = 0; i < queryStringArray.length; i++ ) {
    var keyValueArray = queryStringArray[i].split( '=' );
    if ( keyValueArray[0] === key ) {
      return keyValueArray[1];
    }
  }
  return false;
}

function populateOptions(){
  var top   = getQueryStringValue("top"),
      start = getQueryStringValue("start"),
      end   = getQueryStringValue("end");
  
  if (top === false){
    top = 10;
  } else {
    top = parseInt(top, 0);
  }
  $('#top').val(top);
  
  if (start !== false){
    $('#start').val(start);
  }
  
  if (end !== false){
    $('#end').val(end);
  }
}

function loadTable(data, columns){
  var options = { 
                  aaData: [],
                  aoColumns : [],
                  bPaginate : false,
                  sDom: 'T<"clear">lfrtip'
                }
  $.each(data, function(_, d){
    var arr = [];
    $.each(columns, function(_, c){
      arr.push(d[c]);
    });
    options.aaData.push(arr);
  });
  $.each(columns, function(i, v){
    var cObj = {'sTitle':v};
    if (v == 'id'){
      cObj['mRender'] = function(data, type, row){
        return '<a target="_blank" href="https://moodletest5.vlacs.org/blocks/helpdesk/view.php?id='+data+'">'+data+'</a>';
      };
    }
    options.aoColumns.push(cObj);
  });
  $('#table').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" id="data"></table>' ).show();
  var dataTable = $('#data').dataTable(options);
  new FixedHeader (dataTable, { "zTop": "0" });  
}
function destroyTable(){
  $('#table').empty();
  $('.fixedHeader').remove();
}

$(function () {
  populateOptions();
  $(window).bind('hashchange', function(e) {
    loadChartByHash();
  });
  loadChartByHash();
});
  


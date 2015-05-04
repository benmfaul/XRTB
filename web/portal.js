       function DynamicTable(tableId, cells) {
           this.tableId = tableId;
           this.cells = cells;

       }
       
       DynamicTable.prototype.clear = function() {
        	var table = document.getElementById(this.tableId);
			while(table.rows.length>1) {
               table.deleteRow(1);
            }
        }

       DynamicTable.prototype.draw = function((what, a, b) {
                   var table = document.getElementById(this.tableId);
                   if (what === 'new') {
                       var rowCount = table.rows.length + 1;
                       var row = table.insertRow(-1);
                       row.setAttribute("id", tableId + rowCount);
                       var cell1 = row.insertCell(0);
                       var cell2 = row.insertCell(1);
                       var cell3 = row.insertCell(2);

                       if (a === undefined)
                           value = "?"
                       else
                           value = a;
                       cell1.innerHTML = "<input type='button' value='" + value + "' onclick=\"processCampaign('" + value + "');\"  class='primary-btn col-xs-11 text-left' style=\"width:280px\">";
                       cell2.innerHTML = "<a href=\"javascript:deleteCampaign('" + rowCount + "','" + value + "');\">Del<\/a>";
                   } else {
                       for (var i = 0; i < table.rows.length; i++) {
                           row = table.rows[i];
                           id = row.getAttribute("id");
                           if (id == "row" + what) {
                               table.deleteRow(i);
                               return;
                           }
                       }

                   }
               };
         
 	function DynamicTable(tableId, cells) {
           this.tableId = tableId;
           this.cells = cells;
           this.headers = document.getElementById(tableId).rows.length;
       }
       DynamicTable.prototype.clear = function() {
           var table = document.getElementById(this.tableId);
           while (table.rows.length > this.headers) {
               table.deleteRow(this.headers);
           }
       }
       DynamicTable.prototype.macroSubs = function(line, a, rowCount, idr, col) {
           line = line.replace(/SUB_VALUE/g, a);
           line = line.replace(/SUB_ID/g, this.tableId + ":" + col + ":" +
               rowCount);
           line = line.replace(/ROW_ID/g, idr);
           return line;
       }
       DynamicTable.prototype.remove = function(what) {
           var id = this.tableId;
           var table = document.getElementById(id);
           for (var i = 0; i < table.rows.length; i++) {
               row = table.rows[i];
               id = row.getAttribute("id");
               if (id == what) {
                   table.deleteRow(i);
                   return;
               }
           }
       }
       DynamicTable.prototype.add = function(values) {
           var id = this.tableId;
           var table = document.getElementById(id);
           var rowCount = table.rows.length + 1;
           var row = table.insertRow(-1);
           var label = id + rowCount;
           row.setAttribute("id", id + rowCount);
           for (var i = 0; i < this.cells.length; i++) {
               var cell = row.insertCell(i);
               cell.innerHTML = this.macroSubs(this.cells[i], values[i],
                   rowCount, id + rowCount, i);
               obj = document.getElementById(this.tableId + ":" + i + ":" +
                   rowCount);
               if (obj !== null) {
                   if (obj.tagName == "SELECT") {
                       for (var j = 0; j < obj.options.length; j++) {
                           if (obj.options[j].value === values[i]) {
                               obj.selectedIndex = j;
                               break;
                           }
                       }
                   } else
                   if (obj.tagName == "DIV") {
                       obj.innerHTML = "<img src='" + values[i] + "'/>";
                   } else {
                       if (obj.type == "checkbox") {
                           obj.checked = values[i];
                       } else obj.value = values[i];
                   }
               }
               console.log(cell.innerHTML);
           }
           return label;
       }
       
       DynamicTable.prototype.getObject = function(rowNum, cellNum) {
       		var id = this.tableId;
            var table = document.getElementById(id);
            row = table.rows[rowNum];
            cell = row.cells[cellNum];
            object = cell.children[0];
            return object;
       }
       
       DynamicTable.prototype.numRows = function() {
       		var id = this.tableId;
            var table = document.getElementById(id);
            return table.rows.length;
       }
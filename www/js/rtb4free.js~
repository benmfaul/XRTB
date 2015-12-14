 	

function Logger(tname,logname, spec) {
		var self = this;
		var socket = new WebSocket(spec);
		socket.onopen = function() {
    		socket.send(JSON.stringify(["SUBSCRIBE", logname]));
		};
		socket.onmessage = function(evt) {
			var d = new Date();
			var datestring = d.getDate()  + "-" + (d.getMonth()+1) + "-" + d.getFullYear() + " " +
					d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds()
		 	var x = JSON.parse(evt.data);
   	 		console.log(JSON.stringify(x,null,2));
   	 		var y = x.SUBSCRIBE;
   	 			
   	 		var channel = y[1];
   	 		y = y[2];
   	 		y = JSON.parse(y);
   	 		
   	 		if (typeof y.sev === 'undefined')
   	 			return;

   	 		self.addRow(tname,[datestring,y.sev,y.source,y.field,y.message]);
   	 		console.log("---------------\n"+JSON.stringify(y,null,2));
		}
	}
		
Logger.prototype.addRow =  function(tname,dataCells) {
   	 		var table = document.getElementById(tname);
   	 		var row = table.insertRow(1);
   			for (var i = 0; i < dataCells.length; i++) {
     			var cell = row.insertCell(i);
     			cell.innerHTML = "<td>" + dataCells[i] + "</td>";
			}
		}
 	
 function CommandLogger(tname,logname, spec) {
		var self = this;
		var socket = new WebSocket(spec);
		var logspec = [];
		logspec.push("SUBSCRIBE");
		if (Array.isArray(logname)) {
			for (var i = 0; i < logname.length; i++) {
				logspec.push(logname[i]);
			}
		} else
			logspec.push(logname);
				
		socket.onopen = function() {
    		socket.send(JSON.stringify(logspec));
		};
		socket.onmessage = function(evt) {
	  	 x = JSON.parse(evt.data);
   	 	console.log(JSON.stringify(x,null,2));
   	 	y = x.SUBSCRIBE;
   	 	channel = y[1];
   	 	y = y[2];
   	 	y = JSON.parse(y);
   	 	console.log("---------------\n"+JSON.stringify(y,null,2));
   	 
   	 	if (typeof y.from === 'undefined')
   	 		return;
   
   		var d = new Date();
		var datestring = d.getDate()  + "-" + (d.getMonth()+1) + "-" + d.getFullYear() + " " +
			d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds()

   		var cells = [ datestring, channel, y.from, y.to, y.id, y.name, y.msg ];
    	self.addRow(tname,cells);
    	}
		
}
		
CommandLogger.prototype.addRow =  function(tname,dataCells) {
   	 		var table = document.getElementById(tname);
   	 		var row = table.insertRow(1);
   			for (var i = 0; i < dataCells.length; i++) {
     			var cell = row.insertCell(i);
     			cell.innerHTML = "<td>" + dataCells[i] + "</td>";
			}
		}
 	
 	
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
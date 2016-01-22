 	
function formatDate(d) {
	return [(d.getMonth()+1).padLeft(),
               d.getDate().padLeft(),
               d.getFullYear()].join('/') +' ' +
              [d.getHours().padLeft(),
               d.getMinutes().padLeft(),
               d.getSeconds().padLeft()].join(':');
}

Number.prototype.padLeft = function(base,chr){
    var  len = (String(base || 10).length - String(this).length)+1;
    return len > 0? new Array(len).join(chr || '0')+this : this;
}

function RedisCallback(logname, spec, callback) {
		var self = this;
		
		var previous_response_length = 0;
		var xhr = new XMLHttpRequest()
		xhr.open("GET", "http://" + spec + "/SUBSCRIBE/" + logname, true);
		xhr.onreadystatechange = checkData;
		xhr.send(null);

		function checkData() {
    		if(xhr.readyState == 3)  {
        	response = xhr.responseText;
        	chunk = response.slice(previous_response_length);
        	previous_response_length = response.length;
        	var obj = JSON.parse(chunk);
        	callback(obj);
        	console.log(chunk);
   		 }
		};
	}

function Logger(tname,logname, spec) {
		var self = this;
		
		var previous_response_length = 0;
		var xhr = new XMLHttpRequest()
		xhr.open("GET", "http://" + spec + "/SUBSCRIBE/" + logname, true);
		xhr.onreadystatechange = checkData;
		xhr.send(null);

		function checkData() {
    		if(xhr.readyState == 3)  {
        	response = xhr.responseText;
        	chunk = response.slice(previous_response_length);
        	previous_response_length = response.length;
        	var obj = JSON.parse(chunk);
        	var array = obj.SUBSCRIBE;
        	var y = JSON.parse(array[2]);
        	if (typeof y.sev !== 'undefined') {
        		var d = new Date();
        		var datestring = formatDate(d);
   	 		    self.addRow(tname,[datestring,y.sev,y.source,y.field,y.message]);
        	}
        	console.log(chunk);
   		 }
		};
	}

		
Logger.prototype.addRow =  function(tname,dataCells) {
   	 		var table = document.getElementById(tname);
   	 		var row = table.insertRow(1);
   			for (var i = 0; i < dataCells.length; i++) {
     			var cell = row.insertCell(i);
     			cell.innerHTML = "<td>" + dataCells[i] + "</td>";
			}
			while(table.rows.length > 100) {
				table.deleteRow(100);
			}
		}
 	
 function CommandLogger(tname,logname, spec) {
		var self = this;
		
		var previous_response_length = 0;
		var xhr = new XMLHttpRequest()
		xhr.open("GET", "http://" + spec + "/SUBSCRIBE/" + logname, true);
		xhr.onreadystatechange = checkData;
		xhr.send(null);

		function checkData() {
    		if(xhr.readyState == 3)  {
        	response = xhr.responseText;
        	chunk = response.slice(previous_response_length);
        	previous_response_length = response.length;
        	var obj = JSON.parse(chunk);
        	var array = obj.SUBSCRIBE;
        	var y = JSON.parse(array[2]);
        	if (typeof y.sev !== 'undefined') {
        		var d = new Date();
        		var datestring = formatDate(d);
					
				var cells = [ datestring, channel, y.from, y.to, y.id, y.name, y.msg ];
    			self.addRow(tname,cells);
        	}
        	console.log(chunk);
   		 }
		};			
}
		
CommandLogger.prototype.addRow =  function(tname,dataCells) {
   	 		var table = document.getElementById(tname);
   	 		var row = table.insertRow(1);
   			for (var i = 0; i < dataCells.length; i++) {
     			var cell = row.insertCell(i);
     			cell.innerHTML = "<td>" + dataCells[i] + "</td>";
			}
			while(table.rows.length > 100) {
				table.deleteRow(100);
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

function doModal(etype,title,text) {
    
	BootstrapDialog.show({
                type: etype,
                title: title,
                message: text
            });
}

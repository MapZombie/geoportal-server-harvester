/* 
 * Copyright 2016 Esri, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

define(["dojo/_base/declare",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/i18n!../../nls/resources",
        "dojo/text!./templates/Tasks.html",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/dom-construct",
        "dojo/on",
        "dojo/json",
        "dojo/topic",
        "dijit/Dialog",
        "hrv/rest/Tasks",
        "hrv/ui/tasks/Task",
        "hrv/ui/tasks/TaskEditorPane"
      ],
  function(declare,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin,
           i18n,template,
           lang,array,domConstruct,on,json,topic,
           Dialog,
           TasksREST,Task,TaskEditorPane
          ){
  
    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
      i18n: i18n,
      templateString: template,
    
      postCreate: function(){
        this.load();
      },
      
      load: function() {
        domConstruct.empty(this.contentNode);
        TasksREST.list().then(
          lang.hitch(this,this.processTasks),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",new Error("Unable to access tasks information"));
          })
        );
      },
      
      processTasks: function(response) {
        array.forEach(response,lang.hitch(this,this.processTask));
      },
      
      processTask: function(task) {
        var widget = new Task(task);
        widget.placeAt(this.contentNode);
        on(widget,"remove",lang.hitch(this,this._onRemove));
        on(widget,"run",lang.hitch(this,this._onRun));
        on(widget,"history",lang.hitch(this,this._onHistory));
        widget.startup();
      },
      
      _onRemove: function(evt) {
        TasksREST.delete(evt.data.uuid).then(
          lang.hitch(this,this.load),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",new Error("Error removing task"));
          })
        );
      },
      
      _onRun: function(evt) {
        var uuid = evt.data.uuid;
        TasksREST.execute(uuid).then(
          lang.hitch(this,function(){
            this.load();
          }),
          lang.hitch(this,function(error){
            console.error(error);
            topic.publish("msg",new Error("Error executing task"));
          })
        );
      },
      
      _onAdd: function(evt) {
        var taskEditorPane = new TaskEditorPane({});

        // create editor dialog box
        var taskEditorDialog = new Dialog({
          title: this.i18n.tasks.editor.caption,
          content: taskEditorPane,
          onHide: function() {
            taskEditorDialog.destroy();
            taskEditorPane.destroy();
          }
        });
        
        // listen to "submit" button click
        on(taskEditorPane,"submit",lang.hitch(this, function(evt){
          var taskDefinition = evt.taskDefinition;
          
          // use API create new task
          TasksREST.create(json.stringify(taskDefinition)).then(
            lang.hitch({taskEditorPane: taskEditorPane, taskEditorDialog: taskEditorDialog, self: this},function(){
              this.taskEditorDialog.destroy();
              this.taskEditorPane.destroy();
              this.self.load();
            }),
            lang.hitch(this,function(error){
              console.error(error);
              topic.publish("msg",new Error("Error creating task"));
            })
          );
        }));
        
        taskEditorDialog.show();
      },
      
      _onHistory: function(evt) {
        topic.publish("nav",{type: "history", data: evt.data});
      }
    });
});

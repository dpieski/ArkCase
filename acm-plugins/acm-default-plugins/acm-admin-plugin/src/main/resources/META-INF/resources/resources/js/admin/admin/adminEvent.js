/**
 * Admin.Event
 *
 * event handlers for objects
 *
 * @author jwu
 */
Admin.Event = {
    create : function() {
    }

    ,onClickBtnTest: function(e) {
        alert("test clicked");
        App.Object.setApprovers(null);
        App.Object.setComplaintTypes(null);
        App.Object.setPriorities(null);
    }
    ,onPostInit: function() {
//        Admin.Service.retrieveMyTasks(App.getUserName());
        Admin.Service.retrieveTemplates();
    }

};
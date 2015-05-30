/*global cordova, module*/

var successHandlers = {};
var errorHandlers = {}
var eventHandler;

function successCallback(data) {
    if (data.event && eventHandler) {
        eventHandler(data.event);
    }

    if (data.action && successHandlers[data.action]) {
        successHandlers[data.action]();
    }
}

function errorCallback(data) {
    if (data.action && errorHandlers[data.action]) {
        errorHandlers[data.action](data.error);
    }
}

module.exports = {
    authenticate: function (appUid, appSecret, permissions, successFn, errorFn) {
        successHandlers['authenticate'] = successFn;
        errorHandlers['authenticate'] = errorFn;
        cordova.exec(successCallback, errorCallback, "NeuraNest", "authenticate", [appUid, appSecret , permissions]);
    },
    subscribe: function (actions, successFn, errorFn, eventHandlerFn) {
        successHandlers['subscribe'] = successFn;
        errorHandlers['subscribe'] = errorFn;
        eventHandler = eventHandlerFn;
        cordova.exec(successCallback, errorCallback, "NeuraNest", "subscribe", [actions]);
    }
};

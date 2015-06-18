/*global cordova, module*/
var handlers = {};
var onceHandlers = [];

function fireEvent(event, data) {
    if (onceHandlers[event]) {
        onceHandlers[event].forEach(function (handler) {
            handler(data);
        });
        onceHandlers[event] = [];
    }

    if (handlers[event]) {
        handlers[event].forEach(function (handler) {
            handler(data);
        });
    }
}

function successCallback(data) {
    if (data.event) {
        fireEvent('event', data.event);
        return;
    }

    if (data.action) {
        fireEvent(data.action, {success: true});
    }
}

function errorCallback(data) {
    if (data.action) {
        fireEvent(data.action, {success: false, error: data.error});
    }
}

module.exports = {
    on: function (event, handler) {
        if (!handlers[event]) {
            handlers[event] = [];
        }
        handlers[event].push(handler);
    },

    off: function (event, handler) {
        if (!handlers[event]) {
            return;
        }
        var pos = handlers[event].indexOf(event);
        if (pos === -1) {
            return;
        }

        handlers[event].splice(pos, 1);
    },

    once: function (event, handler) {
        if (!onceHandlers[event]) {
            onceHandlers[event] = [];
        }
        onceHandlers[event].push(handler);
    },


    authenticate: function (appUid, appSecret, permissions) {
        cordova.exec(successCallback, errorCallback, "NeuraNest", "authenticate", [appUid, appSecret , permissions]);
    },

    connect: function () {
        cordova.exec(successCallback, errorCallback, "NeuraNest", "connect", []);
    },
    subscribe: function (actions) {
        cordova.exec(successCallback, errorCallback, "NeuraNest", "subscribe", [actions]);
    }
};

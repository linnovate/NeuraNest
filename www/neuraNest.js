/*global cordova, module*/

module.exports = {
    authenticate: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "NeuraNest", "authenticate", ["12abac932242f5d71f16560aadebf9f97c23cc4be9d71e43d6748792d763a849","1ec478d9ceabe452070bfd0e868fcd4289b891225fd9e5198f5506c2bedbc34b",["userWokeUp", "userArrivedHome"]]);
    },
    subscribe: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "NeuraNest", "subscribe", [["userWokeUp", "userArrivedHome"]]);
    }
};

'use strict';

/**
 * @ngdoc service
 * @name services:Object.NoteService
 *
 * @description
 *
 * {@link /acm-standard-applications/arkcase/src/main/webapp/resources/services/object/object-note.client.service.js services/object/object-note.client.service.js}

 * Object.NoteService includes group of REST calls related to note.
 */
angular.module('services').factory('Object.NoteService', [ '$resource', 'Acm.StoreService', 'UtilService', '$translate', function($resource, Store, Util, $translate) {
    var Service = $resource('api/latest/plugin', {}, {
        /**
         * @ngdoc method
         * @name _queryNotes
         * @methodOf services:Object.NoteService
         *
         * @description
         * Query list of notes for an object.
         *
         * @param {Object} params Map of input parameter
         * @param {String} params.parentType  Object type
         * @param {String} params.parentId  Object ID
         * @param {Function} onSuccess (Optional)Callback function of success query
         * @param {Function} onError (Optional) Callback function when fail
         *
         * @returns {Object} Object returned by $resource
         */
        _queryNotes: {
            method: 'GET',
            url: 'api/latest/plugin/note/:parentType/:parentId',
            cache: false,
            isArray: true
        }

        /**
         * @ngdoc method
         * @name _queryNotes
         * @methodOf services:Object.NoteService
         *
         * @description
         * Query list of notes for an object.
         *
         * @param {Object} params Map of input parameter
         * @param {String} params.parentType  Object type
         * @param {String} params.parentId  Object ID
         * @param {Number} params.start Zero based start number of record
         * @param {Number} params.n Max Number of list to return
         * @param {String} params.sort  Sort value, with format 'sortBy sortDir', sortDir can be 'asc' or 'desc'
         * @param {Function} onSuccess (Optional)Callback function of success query
         * @param {Function} onError (Optional) Callback function when fail
         *
         * @returns {Object} Object returned by $resource
         */
        ,
        _queryNotesPage: {
            method: 'GET',
            url: 'api/latest/plugin/note/:parentType/:parentId/page?type=:type&start=:start&n=:n&s=:sort',
            cache: false,
            isArray: false
        }

        /**
         * @ngdoc method
         * @name _saveNote
         * @methodOf services:Object.NoteService
         *
         * @description
         * Create a new note or update an existing note
         *
         * @param {Object} data Task data
         * @param {Function} onSuccess (Optional)Callback function of success query
         * @param {Function} onError (Optional) Callback function when fail
         *
         * @returns {Object} Object returned by $resource
         */
        ,
        _saveNote: {
            method: 'POST',
            url: 'api/latest/plugin/note/',
            cache: false
        }

        /**
         * @ngdoc method
         * @name _deleteNote
         * @methodOf services:Object.NoteService
         *
         * @description
         * Create a new note or update an existing note
         *
         * @param {Object} params Map of input parameter
         * @param {String} params.noteId  Note ID
         * @param {Function} onSuccess (Optional)Callback function of success query
         * @param {Function} onError (Optional) Callback function when fail
         *
         * @returns {Object} Object returned by $resource
         */
        ,
        _deleteNote: {
            method: 'DELETE',
            url: 'api/latest/plugin/note/:noteId',
            cache: false
        }

        /**
         * @ngdoc method
         * @name _queryNotesByType
         * @methodOf services:Object.NoteService
         *
         * @description
         * Query list of notes by note type.
         *
         * @param {Object} params Map of input parameter
         * @param {String} params.parentType  Object type
         * @param {Number} params.parentId  Object ID
         * @param {String} params.noteType  Note type
         * @param {Function} onSuccess (Optional)Callback function of success query
         * @param {Function} onError (Optional) Callback function when fail
         *
         * @returns {Object} Object returned by $resource
         */
        ,
        _queryNotesByType: {
            method: 'GET',
            url: 'api/latest/plugin/note/:parentType/:parentId?type=:noteType',
            cache: false,
            isArray: true
        }

    });

    Service.SessionCacheNames = {};
    Service.CacheNames = {
        NOTES: "Notes"
    };

    /**
     * @ngdoc method
     * @name queryNotes
     * @methodOf services:Object.NoteService
     *
     * @description
     * Query list of notes of an object
     *
     * @param {String} objectType  Object type
     * @param {Number} objectId  Object ID
     *
     * @returns {Object} Promise
     */
    Service.queryNotes = function(objectType, objectId, noteType) {
        noteType = noteType || "GENERAL";
        var cacheNotes = new Store.CacheFifo(Service.CacheNames.NOTES);
        var cacheKey = objectType + "." + objectId + "." + noteType;
        var notes = cacheNotes.get(cacheKey);
        return Util.serviceCall({
            service: Service._queryNotesByType,
            param: {
                parentType: objectType,
                parentId: objectId,
                noteType: noteType
            },
            result: notes,
            onSuccess: function(data) {
                if (Service.validateNotes(data)) {
                    notes = data;
                    cacheNotes.put(cacheKey, notes);
                    return notes;
                }
            }
        });
    };

    /**
     * @ngdoc method
     * @name queryNotesPage
     * @methodOf services:Object.NoteService
     *
     * @description
     * Query list of notes of an object sorted and paged
     *
     * @param {String} objectType  Object type
     * @param {Number} objectId  Object ID
     * @param {String} noteType  Note type
     * @param {Number} start  Start number of the result list required
     * @param {Number} n  Number of page results
     * @param {String} sortBy  Field the sort will be performed
     * @param {String} sortDir ASC or DESC sorting
     *
     * @returns {Object} Promise
     */
    Service.queryNotesPage = function(objectType, objectId, noteType, start, n, sortBy, sortDir) {
        noteType = noteType || "GENERAL";
        var cacheCaseNoteData = new Store.CacheFifo(Service.CacheNames.NOTES);
        var cacheKey = objectType + "." + objectId + "." + noteType + "." + start + "." + n + "." + sortBy + "." + sortDir;
        var noteData = cacheCaseNoteData.get(cacheKey);

        var sort = "";
        if (!Util.isEmpty(sortBy)) {
            sort = sortBy + " " + Util.goodValue(sortDir, "asc");
        }

        return Util.serviceCall({
            service: Service._queryNotesPage,
            param: {
                parentType: objectType,
                parentId: objectId,
                type: noteType,
                start: start,
                n: n,
                sort: sort
            },
            result: noteData,
            onSuccess: function(data) {
                if (Service.validateNotesData(data)) {
                    noteData = data;
                    cacheCaseNoteData.put(cacheKey, noteData);
                    return noteData;

                }
            }
        });
    };

    /**
     * @ngdoc method
     * @name saveNote
     * @methodOf services:Object.NoteService
     *
     * @description
     * Save note data
     *
     * @param {Object} noteInfo  Note data
     *
     * @returns {Object} Promise
     */
    Service.saveNote = function(noteInfo) {
        if (noteInfo.id && 0 != noteInfo.id) { //Don't validate when creating new note; there is no id yet
            if (!Service.validateNote(noteInfo)) {
                return Util.errorPromise($translate.instant("common.service.error.invalidData"));
            }
        }
        return Util.serviceCall({
            service: Service._saveNote,
            data: noteInfo,
            onSuccess: function(data) {
                if (Service.validateNote(data)) {
                    var noteInfo = data;
                    Service.clearCache(noteInfo.parentType, noteInfo.parentId);
                    return noteInfo;
                }
            }
        });
    };

    /**
     * @ngdoc method
     * @name deleteNote
     * @methodOf services:Object.NoteService
     *
     * @description
     * Delete a note
     *
     * @param {Number} noteId  Note ID
     * @param {Number} parentId  Parent Object ID
     * @param {Number} parentType  Parent Object Type
     *
     * @returns {Object} Promise
     */
    Service.deleteNote = function(noteId, parentId, parentType) {
        return Util.serviceCall({
            service: Service._deleteNote,
            param: {
                noteId: noteId
            },
            data: {},
            onSuccess: function(data) {
                if (Service.validateDeletedNote(data)) {
                    Service.clearCache(parentType, parentId);
                    return data;
                }
            }
        });
    };

    /**
     * @ngdoc method
     * @name validateNotes
     * @methodOf services:Object.NoteService
     *
     * @description
     * Validate notes
     *
     * @param {Object} data  Data to be validated
     *
     * @returns {Boolean} Return true if data is valid
     */
    Service.validateNotes = function(data) {
        if (Util.isEmpty(data)) {
            return false;
        }
        if (!Util.isArray(data)) {
            return false;
        }
        for (var i = 0; i < data.length; i++) {
            if (!this.validateNote(data[i])) {
                return false;
            }
        }
        return true;
    };

    /**
     * @ngdoc method
     * @name validateNote
     * @methodOf services:Object.NoteService
     *
     * @description
     * Validate notes
     *
     * @param {Object} data  Data to be validated
     *
     * @returns {Boolean} Return true if data is valid
     */
    Service.validateNote = function(data) {
        if (Util.isEmpty(data)) {
            return false;
        }
        if (Util.isEmpty(data.id)) {
            return false;
        }
        if (Util.isEmpty(data.parentId)) {
            return false;
        }
        return true;
    };

    /**
     * private method
     * name validateDeletedNote
     * methodOf services:Object.NoteService
     *
     * @description
     * Validate response of deleted note.
     *
     * @param {Object} data  Data to be validated
     *
     * @returns {Boolean} Return true if data is valid
     */
    Service.validateDeletedNote = function(data) {
        if (Util.isEmpty(data)) {
            return false;
        }
        if (Util.isEmpty(data.deletedNoteId)) {
            return false;
        }
        return true;
    };

    /**
     * @ngdoc method
     * @name validateNotesData
     * @methodOf services:Object.NoteService
     *
     * @description
     * Validate note data
     *
     * @param {Object} data  Data to be validated
     *
     * @returns {Boolean} Return true if data is valid
     */
    Service.validateNotesData = function(data) {
        if (!Util.isArray(data.resultPage)) {
            return false;
        }
        for (var i = 0; i < data.resultPage.length; i++) {
            if (!this.validateNote(data.resultPage[i])) {
                return false;
            }
        }
        if (Util.isEmpty(data.totalCount)) {
            return false;
        }
        return true;
    };

    /**
     * @ngdoc method
     * @name clearCache
     * @methodOf services:Object.NoteService
     *
     * @description
     * Clear cache for notes 
     *
     * @param {String} objectType  Object type
     * @param {Number} objectId  Object ID  
     *
     * @returns [Undefined] don't return anything.
     */
    Service.clearCache = function(objectType, objectId) {
        var cacheNotes = new Store.CacheFifo(Service.CacheNames.NOTES);
        var cacheSubKey = objectType + "." + objectId;
        var cacheKeys = cacheNotes.keys();
        _.each(cacheKeys, function(key) {
            if (key == null) {
                return;
            }
            if (key.indexOf(cacheSubKey) >= 0) {
                cacheNotes.remove(key);
            }
        });
    };

    return Service;
} ]);

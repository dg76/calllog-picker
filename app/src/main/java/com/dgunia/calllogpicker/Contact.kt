package com.dgunia.calllogpicker

class Contact {
    var contactId: String
    var displayName: String
    var givenName: String? = null
    var middleName: String? = null
    var familyName: String? = null

    constructor(contactId: String, displayName: String) {
        this.contactId = contactId
        this.displayName = displayName
    }

    constructor(contactId: String, displayName: String, givenName: String, middleName: String, familyName: String) {
        this.contactId = contactId
        this.displayName = displayName
        this.givenName = givenName
        this.middleName = middleName
        this.familyName = familyName
    }
}
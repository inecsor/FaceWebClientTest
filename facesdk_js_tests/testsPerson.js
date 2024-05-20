const {faceSdk, readImageBytes, ...paths} = require('./misc/pathsAndUrls');
const chai = require('chai');
const assert = chai.assert;
const fs = require('fs');


describe('Person Tests', function () {

    let testGroupName = "testGroup";
    let baseMetadata = {'description': 'This is a test group'}
    let nameA = 'Person A'
    let nameB = 'Person B'
    let groupId, personId;
    let createdPersonResponse

    beforeEach(async function () {
        const groupToCreate = {
            name: testGroupName,
            metadata: baseMetadata
        };
        const createdGroup = await faceSdk.groupApi.createGroup(groupToCreate);
        groupId = createdGroup.id;
        const personFields = {
            name: nameA,
            groups: [groupId],
            metadata: baseMetadata
        };
        createdPersonResponse = await faceSdk.personApi.createPerson(personFields);
        assert.ok(createdPersonResponse, "No response received");
        personId = createdPersonResponse.id;
    });

    afterEach(async function () {
        try {
            await faceSdk.groupApi.deleteGroup(groupId);
        } catch (e) {
        }
        try {
            await faceSdk.personApi.deletePerson(personId);
        } catch (e) {
        }
    });

    it('should create a person', async function () {
        assert.strictEqual(nameA, createdPersonResponse.name,
            `Expected person Name to be ${nameA}, but got ${createdPersonResponse.name}`)
        assert.strictEqual(groupId, createdPersonResponse.groups[0],
            `Expected person groupId to be ${groupId}, but got ${createdPersonResponse.groups[0]}`)
        assert.strictEqual(baseMetadata['description'], createdPersonResponse.metadata['description'],
            `Expected person metadata description to be ${baseMetadata['description']}, 
            but got ${createdPersonResponse.metadata['description']}`)
    });

    it('should get a person', async function () {

        const retrievedPerson = await faceSdk.personApi.getPerson(personId);

        assert.ok(retrievedPerson, "No response received");
        assert.strictEqual(retrievedPerson.id, personId, `Expected person ID to be ${personId}, but got ${retrievedPerson.id}`);
    });

    it('should update a person', async function () {
        const updatedPersonFields = {
            name: nameB,
            metadata: {'description': 'upd'}
        };
        await faceSdk.personApi.updatePerson(personId, updatedPersonFields);
        const updatedPerson = await faceSdk.personApi.getPerson(personId);
        assert.strictEqual(updatedPerson.name, nameB, `Expected name to be ${nameB}, but got ${updatedPerson.name}`);
        assert.strictEqual(updatedPerson.metadata.description, 'upd', `Expected description to be 'upd'`);
    });

    it('should delete a person', async function () {
        await faceSdk.personApi.deletePerson(personId);
        const personsResponse = await faceSdk.groupApi.getAllPersonsByGroupId(1, 1, groupId);
        assert.deepStrictEqual(personsResponse.items, [], `Expected 'items' to be empty, but got ${personsResponse.items}`);
    });

    it('should get person images', async function () {
        const image = fs.readFileSync(paths.face1Path);
        const imageFields = {
            image: {
                content: image,
                contentType: 'image/jpeg' // Replace with the actual mime type if different
            }
        };
        await faceSdk.personApi.addImageToPerson(personId, imageFields);
        const imagesResponse = await faceSdk.personApi.getAllImagesByPersonId(1, 1, personId);
        assert.ok(imagesResponse.items, "'items' key not found in the response");
        assert.ok(imagesResponse.items[0]['id'], 'id of photo not found')
        assert.ok(imagesResponse.items[0]['path'], 'path of photo not found')
    });

    it('should get all groups associated with a person', async function () {
        let testGroupName2 = "secondTestGroup"
        const groupToCreate = {
            name: testGroupName2,
            metadata: baseMetadata
        };
        const secondGroup = await faceSdk.groupApi.createGroup(groupToCreate);
        let secondGroupId = secondGroup.id;
        const personFields = {
            name: nameA,
            groups: [groupId, secondGroupId],
            metadata: baseMetadata

        };
        const newPerson = await faceSdk.personApi.createPerson(personFields);
        const newPersonId = newPerson.id;
        const groupsResponse = await faceSdk.personApi.getAllGroupsByPersonId(1, 2, newPersonId);
        const groupNames = groupsResponse.items.map(group => group.name);
        assert.strictEqual(groupsResponse.items.length, 2, `Expected 2 items, but got ${groupsResponse.items.length}`);
        assert.ok(groupNames.includes(testGroupName), `${testGroupName} not found in group names`);
        assert.ok(groupNames.includes(testGroupName2), `${testGroupName2} not found in group names`);
        await faceSdk.groupApi.deleteGroup(secondGroupId);
    });

    it('should add an image to person', async function () {
        const image = fs.readFileSync(paths.face1Path);
        const imageFields = {
            image: {
                content: image,
                contentType: 'image/jpeg' // Replace with the actual mime type if different
            }
        };
        const response = await faceSdk.personApi.addImageToPerson(personId, imageFields);
        // Assertions for the returned response
        assert.strictEqual(response.contentType, 'image/jpeg', `Expected contentType to be 'image/jpeg', but got ${response.contentType}`);
        assert.isNotNull(response.id, 'Image ID should not be null');
        const updatedPerson = await faceSdk.personApi.getPerson(personId);
        assert.isNotNull(updatedPerson, "No response received");
        assert.strictEqual(updatedPerson.id, personId, `Expected personId to be ${personId}, but got ${updatedPerson.id}`);
    });


    it('should create person without metadata', async function () {
        const personFields = {
            name: nameA,
            groups: [groupId],
        };
        const newPerson = await faceSdk.personApi.createPerson(personFields);
        const newPersonId = newPerson.id;
        await faceSdk.personApi.deletePerson(newPersonId);
    });
});
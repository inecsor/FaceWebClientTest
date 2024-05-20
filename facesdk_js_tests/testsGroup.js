const {faceSdk} = require("./misc/pathsAndUrls");
const chai = require('chai');
const assert = chai.assert;


describe('Group tests', function () {

    let groupId;
    let createdGroupResponse
    let testGroupName = "test"
    let baseMetadata = {'description': 'This is a test group'}
    let nameA = "Person A"

    this.beforeEach(async function () {
        const groupToCreate = {
            name: testGroupName,
        };
        createdGroupResponse = await faceSdk.groupApi.createGroup(groupToCreate);
        groupId = createdGroupResponse.id;
    });


    this.afterEach(async function () {
        try {
            const deleteResponse = await faceSdk.groupApi.deleteGroup(groupId);
            assert.strictEqual(deleteResponse, undefined, `Unexpected response: ${deleteResponse}, 
            problem with group deletion`);
        } catch (e) {
        }
    });


    it('should create and delete group', async function () {
        // We don't need to create group because it's already created via beforeEach.
        // We just need to check creation response, deletion response will be checked in AfterEach
        assert.ok(createdGroupResponse, "No response received");
        assert.strictEqual(createdGroupResponse.name, testGroupName, "Name of the group is incorrect");
    });


    it('should get all groups', async function () {
        // Step 1: Make a list to store the ids of the created groups
        const createdGroupIds = [];  // Include the groupId created in beforeEach

        // Step 2: Creation of additional groups
        const groupToCreate = {name: testGroupName, metadata: baseMetadata};
        for (let i = 1; i < 3; i++) {
            // Create a group and get its id from response
            const createResponse = await faceSdk.groupApi.createGroup(groupToCreate);
            const additionalGroupId = createResponse.id;
            assert.ok(additionalGroupId, "No id field in the returned group");
            // Store the groupId for later deletion
            createdGroupIds.push(additionalGroupId);
        }
        // Step 3: Get all groups
        const response = await faceSdk.groupApi.getAllGroups(2, 2);

        for (const id of createdGroupIds) {
            await faceSdk.groupApi.deleteGroup(id);
        }
        // Step 4: check that page 2 with 2 items exist
        assert.ok(response, "No response received");
        assert.strictEqual(response.items.length, 2, `Expected 2 items on page, but got ${response.items.length} items`);
        assert.strictEqual(response.page, 2, `Expected page to be 2, but got ${response.page}`);
    });

    it('should get all persons by group id', async function () {
        // Step 2: Adding person to the created group
        const personFields = {
            name: nameA,
            groups: [groupId],
            metadata: baseMetadata
        };
        await faceSdk.personApi.createPerson(personFields);

        // Step 3: Get persons from created groupId
        const personsResponse = await faceSdk.groupApi.getAllPersonsByGroupId(1, 1, groupId);
        assert.ok(personsResponse, "No response received");
        assert.strictEqual(personsResponse.items[0].name, nameA, "Name of Person is incorrect");
        assert.strictEqual(personsResponse.items[0].metadata['description'], baseMetadata['description'],
            "Metadata of the group is incorrect");
        assert.strictEqual(personsResponse.items[0].groups[0], groupId, "group_id is incorrect");
    });

    it('should get a group by its id', async function () {
        // Get all groups and take the first group_id that appears
        const groupsResponse = await faceSdk.groupApi.getAllGroups(1, 1);
        assert.ok(groupsResponse.items.length > 0, "No groups found");
        const firstGroupId = groupsResponse.items[0].id;

        // Using obtained id to get chosen group
        const groupResponse = await faceSdk.groupApi.getGroup(firstGroupId);
        assert.ok(groupResponse, "No response received");
        assert.strictEqual(groupResponse.id, firstGroupId, "group_id is incorrect");
    });

    it('should update a group', async function () {
        // Updating group with new name
        const new_name = 'updated_test_group';
        const updated_metadata = {'description': 'updated meta'};
        const groupToCreate = {name: new_name, metadata: updated_metadata};
        await faceSdk.groupApi.updateGroup(groupId, groupToCreate);

        const updatedGroupResponse = await faceSdk.groupApi.getGroup(groupId);
        assert.strictEqual(updatedGroupResponse.name, new_name, `Group name not updated: ${updatedGroupResponse.name}`);
        assert.strictEqual(updatedGroupResponse.metadata['description'], updated_metadata['description'],
            `Group metadata not updated: ${updatedGroupResponse.metadata['description']}`);
    });


    it('should update persons in group', async function () {
        // Creating some persons and adding them to the group
        const personFields = {
            name: nameA,
            groups: [groupId],
            metadata: baseMetadata
        };
        await faceSdk.personApi.createPerson(personFields);
        // Getting the group's persons
        const initialPersonsResponse = await faceSdk.groupApi.getAllPersonsByGroupId(1, 10, groupId);
        const initialPersonIds = initialPersonsResponse.items.map(person => person.id);
        // Removing them
        await faceSdk.groupApi.updatePersonsInGroup(groupId, {removeItems: initialPersonIds});
        // Creating new persons and adding them to the group
        const newPersonNames = ["New Person A", "New Person B"];
        const newPersonIds = [];
        for (const name of newPersonNames) {
            const personFields = {
                name: name,
                groups: [groupId],
                metadata: baseMetadata
            };
            const createdPersonResponse = await faceSdk.personApi.createPerson(personFields);
            newPersonIds.push(createdPersonResponse.id);
        }
        // Testing that they're here
        const updatedPersonsResponse = await faceSdk.groupApi.getAllPersonsByGroupId(1, 10, groupId);
        const updatedPersonIds = updatedPersonsResponse.items.map(person => person.id);
        const updatedPersonNames = updatedPersonsResponse.items.map(person => person.name);
        // Sort the arrays before comparing
        const sortedUpdatedPersonIds = [...updatedPersonIds].sort();
        const sortedNewPersonIds = [...newPersonIds].sort();
        const sortedUpdatedPersonNames = [...updatedPersonNames].sort();
        const sortedNewPersonNames = [...newPersonNames].sort();

        assert.deepStrictEqual(sortedUpdatedPersonIds, sortedNewPersonIds,
            `Updated person ids don't match: ${sortedUpdatedPersonIds} != ${sortedNewPersonIds}`);
        assert.deepStrictEqual(sortedUpdatedPersonNames, sortedNewPersonNames,
            `Updated person ids don't match: ${sortedUpdatedPersonNames} != ${sortedNewPersonNames}`);
        assert.strictEqual(updatedPersonIds.length, 2,
            `Expected 2 persons in updated list but got ${updatedPersonIds.length}`);
    });

    it('should create group without metadata', async function () {
        const groupToCreate = {
            name: testGroupName,
        };
        createdGroupResponse = await faceSdk.groupApi.createGroup(groupToCreate);
        let groupIdNew = createdGroupResponse.id;
        await faceSdk.groupApi.deleteGroup(groupIdNew);
    });

});




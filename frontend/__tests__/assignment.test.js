const { JSDOM } = require("jsdom");

let document;
let window;
let localStorageMock;

beforeEach(() => {
    const dom = new JSDOM(`
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <title>Assignment Test</title>
        </head>
        <body>
            <table id="submissionsTable">
                <tbody></tbody>
            </table>
            <input id="assignmentName" value="" />
            <input id="AvailableFrom" value="2024-12-01" />
            <input id="Due" value="2024-12-12" />
            <p id="errorMsg" style="display: none;"></p>
        </body>
        </html>
    `);
    document = dom.window.document;
    window = dom.window;
    localStorageMock = {
        getItem: jest.fn(),
        setItem: jest.fn(),
    };
    window.localStorage = localStorageMock;
});

test("should load assignments from localStorage", () => {
    const assignments = [
        { name: "Lab 1", availableFrom: "2024-12-01", due: "2024-12-12", status: "OPEN" },
    ];
    localStorageMock.getItem.mockReturnValue(JSON.stringify(assignments));

    const submissionsTable = document.getElementById("submissionsTable");
    const loadAssignmentsFromLocalStorage = () => {
        const savedAssignments = JSON.parse(window.localStorage.getItem("assignments") || "[]");
        const tbody = submissionsTable.querySelector("tbody");
        savedAssignments.forEach(assignment => {
            const row = tbody.insertRow();
            row.innerHTML = `
                <td></td>
                <td>${assignment.name}</td>
                <td>${assignment.availableFrom}</td>
                <td>${assignment.due}</td>
                <td>${assignment.status}</td>
            `;
        });
    };

    loadAssignmentsFromLocalStorage();

    expect(submissionsTable.querySelector("tbody").children.length).toBe(1);
    expect(submissionsTable.querySelector("tbody").children[0].children[1].textContent).toBe("Lab 1");
});

test("should show error if assignment name is empty", () => {
    const errorMsg = document.getElementById("errorMsg");
    const createBtn = document.createElement("button");
    createBtn.onclick = () => {
        const name = document.getElementById("assignmentName").value.trim();
        if (!name) {
            errorMsg.style.display = "block";
            errorMsg.textContent = "Assignment name is required.";
        }
    };

    document.body.appendChild(createBtn);
    createBtn.click();

    expect(errorMsg.style.display).toBe("block");
    expect(errorMsg.textContent).toBe("Assignment name is required.");
});

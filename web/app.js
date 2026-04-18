const apiBase = "/api/students";
const tableBody = document.querySelector('#studentTable tbody');
const messageBox = document.querySelector('#message');
const modal = document.querySelector('#modal');
const form = document.querySelector('#studentForm');
const modalTitle = document.querySelector('#modalTitle');
const idInput = document.querySelector('#studentId');
const nameInput = document.querySelector('#studentName');
const marksInput = document.querySelector('#studentMarks');
const typeSelect = document.querySelector('#studentType');
const extraInput = document.querySelector('#studentExtra');
const extraLabel = document.querySelector('#extraLabel');

let editingId = null;

window.addEventListener('DOMContentLoaded', () => {
    document.querySelector('#refreshButton').addEventListener('click', loadStudents);
    document.querySelector('#newButton').addEventListener('click', () => openForm());
    document.querySelector('#searchButton').addEventListener('click', searchStudent);
    document.querySelector('#cancelButton').addEventListener('click', closeForm);
    typeSelect.addEventListener('change', updateExtraField);
    form.addEventListener('submit', saveStudent);

    loadStudents();
});

async function loadStudents() {
    try {
        const response = await fetch(apiBase);
        const students = await response.json();
        renderTable(students);
        showMessage('Loaded student records.', '');
    } catch (error) {
        showMessage('Unable to load students: ' + error.message, 'error');
    }
}

function renderTable(students) {
    tableBody.innerHTML = '';
    students.forEach(student => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${student.id}</td>
            <td>${student.name}</td>
            <td>${student.marks.toFixed(2)}</td>
            <td>${student.grade || ''}</td>
            <td>${student.type}</td>
            <td>${detailsText(student)}</td>
            <td>
                <button onclick="editStudent(${student.id})">Edit</button>
                <button class="secondary" onclick="deleteStudent(${student.id})">Delete</button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function detailsText(student) {
    if (student.type === 'Undergraduate') {
        return `Major: ${student.major || ''}`;
    }
    if (student.type === 'Graduate') {
        return `Thesis: ${student.thesisTitle || ''}`;
    }
    return '';
}

function openForm(student = null) {
    editingId = null;
    modalTitle.textContent = student ? 'Update Student' : 'Add Student';
    idInput.value = student ? student.id : '';
    idInput.disabled = !!student;
    nameInput.value = student ? student.name : '';
    marksInput.value = student ? student.marks : '';
    typeSelect.value = student ? student.type : 'Regular';
    extraInput.value = student ? (student.major || student.thesisTitle || '') : '';
    updateExtraField();
    modal.classList.remove('hidden');
    if (student) {
        editingId = student.id;
    }
}

function closeForm() {
    modal.classList.add('hidden');
    editingId = null;
    form.reset();
}

function updateExtraField() {
    const type = typeSelect.value;
    if (type === 'Undergraduate') {
        extraLabel.textContent = 'Major';
        extraInput.disabled = false;
    } else if (type === 'Graduate') {
        extraLabel.textContent = 'Thesis Title';
        extraInput.disabled = false;
    } else {
        extraLabel.textContent = 'Major / Thesis';
        extraInput.value = '';
        extraInput.disabled = true;
    }
}

async function saveStudent(event) {
    event.preventDefault();
    const student = {
        id: Number(idInput.value),
        name: nameInput.value.trim(),
        marks: Number(marksInput.value),
        type: typeSelect.value,
        major: typeSelect.value === 'Undergraduate' ? extraInput.value.trim() : undefined,
        thesisTitle: typeSelect.value === 'Graduate' ? extraInput.value.trim() : undefined
    };

    try {
        const options = {
            method: editingId === null ? 'POST' : 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(student)
        };
        const url = editingId === null ? apiBase : `${apiBase}/${editingId}`;
        const response = await fetch(url, options);
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Unable to save student.');
        }
        closeForm();
        loadStudents();
        showMessage(editingId === null ? 'Student added successfully.' : 'Student updated successfully.', '');
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

async function editStudent(id) {
    try {
        const response = await fetch(`${apiBase}/${id}`);
        if (!response.ok) {
            throw new Error('Student not found');
        }
        const student = await response.json();
        student.grade = student.grade || '';
        openForm(student);
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

async function deleteStudent(id) {
    if (!confirm(`Delete student with ID ${id}?`)) {
        return;
    }
    try {
        const response = await fetch(`${apiBase}/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error('Unable to delete student.');
        }
        loadStudents();
        showMessage('Student deleted successfully.', '');
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

async function searchStudent() {
    const idValue = document.querySelector('#searchInput').value.trim();
    if (!idValue) {
        showMessage('Please enter a student ID.', 'error');
        return;
    }
    try {
        const response = await fetch(`${apiBase}/${idValue}`);
        if (!response.ok) {
            throw new Error('Student not found.');
        }
        const student = await response.json();
        renderTable([student]);
        showMessage('Search results loaded.', '');
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

function showMessage(text, type) {
    messageBox.textContent = text;
    messageBox.style.color = type === 'error' ? 'var(--danger)' : 'var(--text)';
}

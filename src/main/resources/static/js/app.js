/* Hostel Room Allocation Management System (HRAMS) Web JavaScript Engine */

document.addEventListener("DOMContentLoaded", () => {
    // Check if session exists in localStorage
    const savedUser = localStorage.getItem("hrams_user");
    if (savedUser) {
        showAppPortal(savedUser);
    }
});

function handleWebLogin(event) {
    event.preventDefault();
    const user = document.getElementById("username").value.trim();
    const pass = document.getElementById("password").value.trim();
    const errLabel = document.getElementById("loginError");
    const btnSubmit = document.getElementById("btnLoginSubmit");

    errLabel.style.color = "#38bdf8";
    errLabel.innerText = "Authenticating...";
    btnSubmit.disabled = true;

    fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: user, password: pass })
    })
    .then(res => res.json())
    .then(data => {
        btnSubmit.disabled = false;
        if (data.success) {
            errLabel.innerText = "";
            localStorage.setItem("hrams_user", data.username);
            showAppPortal(data.username);
        } else {
            errLabel.style.color = "#f87171";
            errLabel.innerText = data.message || "Invalid credentials.";
        }
    })
    .catch(err => {
        btnSubmit.disabled = false;
        errLabel.style.color = "#f87171";
        errLabel.innerText = "Connection Error: " + err.message;
    });
}

function showAppPortal(username) {
    document.getElementById("loginOverlay").classList.add("hidden");
    document.getElementById("appContainer").classList.remove("hidden");
    document.getElementById("adminUserTag").innerText = "Admin: " + username;
    loadDashboardStats();
}

function handleWebLogout() {
    localStorage.removeItem("hrams_user");
    document.getElementById("appContainer").classList.add("hidden");
    document.getElementById("loginOverlay").classList.remove("hidden");
}

function switchNav(viewId, btnElement) {
    document.querySelectorAll(".view-panel").forEach(p => p.classList.add("hidden"));
    document.querySelectorAll(".nav-item").forEach(b => b.classList.remove("active"));
    
    document.getElementById(viewId).classList.remove("hidden");
    btnElement.classList.add("active");

    const titles = {
        'dashboardView': 'Dashboard Overview',
        'studentsView': 'Student Records Management',
        'roomsView': 'Hostel Room Directory',
        'allocationView': 'Room Allocation & Vacate Management',
        'waitingListView': 'FIFO Waiting Queue Management',
        'reportsView': 'Occupancy & Allocation Reports'
    };
    document.getElementById("pageHeaderTitle").innerText = titles[viewId] || "HRAMS Portal";

    if (viewId === 'dashboardView') loadDashboardStats();
    if (viewId === 'studentsView') loadStudents();
    if (viewId === 'roomsView') loadRooms();
    if (viewId === 'allocationView') loadAllocations();
    if (viewId === 'waitingListView') loadWaitingList();
    if (viewId === 'reportsView') loadReport();
}

/* 1. Dashboard Logic */
function loadDashboardStats() {
    fetch("/api/dashboard/stats")
    .then(res => res.json())
    .then(data => {
        document.getElementById("lblTotalStudents").innerText = data.totalStudents || 0;
        document.getElementById("lblAvailableRooms").innerText = data.availableRooms || 0;
        document.getElementById("lblOccupiedRooms").innerText = data.occupiedRooms || 0;
        document.getElementById("lblWaitingStudents").innerText = data.waitingStudents || 0;
        document.getElementById("lblActiveCount").innerText = "Total Active: " + (data.activeAllocations ? data.activeAllocations.length : 0);
        document.getElementById("dbBannerTag").innerText = "Active DB: " + (data.databaseType || "Spring Boot");

        renderAllocationsTable(data.activeAllocations || [], "tblDashboardAllocations", false);
    });
}

/* 2. Student Logic */
function loadStudents() {
    const q = document.getElementById("txtStudentSearch").value.trim();
    const url = q ? `/api/students?query=${encodeURIComponent(q)}` : "/api/students";
    fetch(url)
    .then(res => res.json())
    .then(students => {
        const tbody = document.getElementById("tblStudentsBody");
        tbody.innerHTML = "";
        students.forEach(s => {
            tbody.innerHTML += `
                <tr>
                    <td>${s.studentId}</td>
                    <td>${s.name}</td>
                    <td>${s.course}</td>
                    <td>${s.yearOfStudy}</td>
                    <td>${s.gender}</td>
                    <td>${s.email}</td>
                    <td>
                        <button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="deleteStudent('${s.studentId}')">Delete</button>
                    </td>
                </tr>
            `;
        });
    });
}

function handleSaveStudent(event) {
    event.preventDefault();
    const student = {
        studentId: document.getElementById("stuId").value.trim(),
        name: document.getElementById("stuName").value.trim(),
        email: document.getElementById("stuEmail").value.trim(),
        phone: document.getElementById("stuPhone").value.trim(),
        course: document.getElementById("stuCourse").value.trim(),
        yearOfStudy: parseInt(document.getElementById("stuYear").value),
        gender: document.getElementById("stuGender").value
    };

    fetch("/api/students", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(student)
    })
    .then(res => res.json())
    .then(data => {
        alert(data.message);
        if (data.success) {
            loadStudents();
            document.getElementById("stuId").value = "";
            document.getElementById("stuName").value = "";
        }
    });
}

function deleteStudent(id) {
    if (confirm("Delete student " + id + "?")) {
        fetch(`/api/students/${id}`, { method: "DELETE" })
        .then(res => res.json())
        .then(data => {
            alert(data.message);
            loadStudents();
        });
    }
}

/* 3. Room Logic */
function loadRooms() {
    fetch("/api/rooms")
    .then(res => res.json())
    .then(rooms => {
        const tbody = document.getElementById("tblRoomsBody");
        tbody.innerHTML = "";
        rooms.forEach(r => {
            let badgeClass = "badge-green";
            let statusText = "Available";
            if (r.occupiedCount >= r.capacity) {
                badgeClass = "badge-red";
                statusText = "Full";
            } else if (r.occupiedCount > 0) {
                badgeClass = "badge-amber";
                statusText = "Partially Occupied";
            }
            tbody.innerHTML += `
                <tr>
                    <td>${r.roomId}</td>
                    <td>${r.roomNumber}</td>
                    <td>${r.floor}</td>
                    <td>${r.roomType}</td>
                    <td>${r.capacity}</td>
                    <td>${r.occupiedCount}</td>
                    <td><span class="badge ${badgeClass}">${statusText}</span></td>
                    <td>
                        <button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="deleteRoom(${r.roomId})">Delete</button>
                    </td>
                </tr>
            `;
        });
    });
}

function handleSaveRoom(event) {
    event.preventDefault();
    const room = {
        roomNumber: document.getElementById("rmNumber").value.trim(),
        floor: parseInt(document.getElementById("rmFloor").value),
        capacity: parseInt(document.getElementById("rmCapacity").value),
        roomType: document.getElementById("rmType").value
    };

    fetch("/api/rooms", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(room)
    })
    .then(res => res.json())
    .then(data => {
        alert(data.message);
        if (data.success) {
            loadRooms();
            document.getElementById("rmNumber").value = "";
        }
    });
}

function deleteRoom(id) {
    if (confirm("Delete room ID " + id + "?")) {
        fetch(`/api/rooms/${id}`, { method: "DELETE" })
        .then(res => res.json())
        .then(data => {
            alert(data.message);
            loadRooms();
        });
    }
}

/* 4. Allocation Engine Logic */
function loadAllocations() {
    fetch("/api/allocations/active")
    .then(res => res.json())
    .then(allocs => {
        renderAllocationsTable(allocs, "tblAllocationsBody", true);
    });
}

function renderAllocationsTable(allocs, tbodyId, showActions) {
    const tbody = document.getElementById(tbodyId);
    tbody.innerHTML = "";
    allocs.forEach(a => {
        const roomDisp = a.roomNumber ? `${a.roomNumber} (${a.roomOccupied}/${a.roomCapacity} Beds)` : "";
        const actionTd = showActions ? `<td><button class="btn btn-danger" style="padding: 4px 8px; font-size: 11px;" onclick="vacateAllocation(${a.allocationId})">Vacate</button></td>` : "";
        tbody.innerHTML += `
            <tr>
                <td>${a.allocationId}</td>
                <td>${a.studentId}</td>
                <td>${a.studentName || ""}</td>
                <td>${roomDisp}</td>
                <td style="color: #38bdf8;">${a.coOccupants || ""}</td>
                <td>${a.allocationDate ? a.allocationDate.replace("T", " ").substring(0, 19) : ""}</td>
                <td><span class="badge badge-green">${a.status}</span></td>
                ${actionTd}
            </tr>
        `;
    });
}

function handleTriggerAllocation() {
    const studentId = document.getElementById("allocStudentId").value.trim();
    if (!studentId) {
        alert("Please enter a Student ID to allocate.");
        return;
    }

    fetch("/api/allocations/trigger", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ studentId: studentId })
    })
    .then(res => res.json())
    .then(data => {
        alert(data.message);
        loadAllocations();
        document.getElementById("allocStudentId").value = "";
    });
}

function vacateAllocation(id) {
    if (confirm("Vacate allocation ID " + id + "?")) {
        fetch(`/api/allocations/vacate/${id}`, { method: "POST" })
        .then(res => res.json())
        .then(data => {
            alert(data.message);
            loadAllocations();
        });
    }
}

/* 5. Waiting List Logic */
function loadWaitingList() {
    fetch("/api/waiting-list")
    .then(res => res.json())
    .then(list => {
        const tbody = document.getElementById("tblWaitingBody");
        tbody.innerHTML = "";
        list.forEach((w, idx) => {
            tbody.innerHTML += `
                <tr>
                    <td>#${idx + 1}</td>
                    <td>${w.waitingId}</td>
                    <td>${w.studentId}</td>
                    <td>${w.studentName || ""}</td>
                    <td>${w.studentCourse || ""}</td>
                    <td>${w.studentGender || ""}</td>
                    <td>${w.requestDate ? w.requestDate.replace("T", " ").substring(0, 19) : ""}</td>
                    <td><span class="badge badge-amber">${w.status}</span></td>
                </tr>
            `;
        });
    });
}

function handleProcessNextWaiting() {
    fetch("/api/waiting-list/process-next", { method: "POST" })
    .then(res => res.json())
    .then(data => {
        alert(data.message);
        loadWaitingList();
    });
}

/* 6. Reports Logic */
function loadReport() {
    const type = document.getElementById("reportTypeSelect").value;
    fetch(`/api/reports/${type}`)
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            document.getElementById("reportTitle").innerText = data.title;
            document.getElementById("reportContent").value = data.content;
        } else {
            alert(data.message);
        }
    });
}

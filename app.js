javascript 

// TaskFlow Frontend - Connects to Java Backend 

// CHANGE THIS TO YOUR COMPUTER'S IP ADDRESS! 

const API_BASE_URL = 'http://YOUR_IP_ADDRESS:4567/api'; 

 

let tasks = []; 

let currentFilter = 'all'; 

 

document.addEventListener('DOMContentLoaded', () => { 

    checkServerStatus(); 

    document.getElementById('taskForm').addEventListener('submit', addTask); 

     

    document.querySelectorAll('.filter-btn').forEach(btn => { 

        btn.addEventListener('click', (e) => { 

            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active')); 

            e.target.classList.add('active'); 

            currentFilter = e.target.dataset.filter; 

            displayTasks(); 

        }); 

    }); 

}); 

 

async function checkServerStatus() { 

    const statusDiv = document.getElementById('serverStatus'); 

    statusDiv.innerHTML = '<span class="status-dot"></span> Connecting to Java server...'; 

     

    try { 

        const response = await fetch(`${API_BASE_URL}/tasks`); 

        if (response.ok) { 

            statusDiv.innerHTML = '<span class="status-dot connected"></span> ✅ Connected to Java Server!'; 

            loadTasks(); 

        } 

    } catch (error) { 

        statusDiv.innerHTML = '<span class="status-dot disconnected"></span> ⚠️ Java Server Offline - Start backend first!'; 

    } 

} 

 

async function loadTasks() { 

    try { 

        const response = await fetch(`${API_BASE_URL}/tasks`); 

        tasks = await response.json(); 

        displayTasks(); 

    } catch (error) { 

        console.error('Error loading tasks:', error); 

    } 

} 

 

function displayTasks() { 

    const taskList = document.getElementById('taskList'); 

    let filteredTasks = tasks; 

     

    if (currentFilter === 'pending') { 

        filteredTasks = tasks.filter(t => t.completed === 0); 

    } else if (currentFilter === 'completed') { 

        filteredTasks = tasks.filter(t => t.completed === 1); 

    } 

     

    if (filteredTasks.length === 0) { 

        taskList.innerHTML = '<div class="empty-message">✨ No tasks found! Add your first task.</div>'; 

        return; 

    } 

     

    taskList.innerHTML = filteredTasks.map(task => ` 

        <div class="task-card ${task.completed === 1 ? 'completed' : ''}"> 

            <div class="task-header"> 

                <div class="task-title">${escapeHtml(task.title)}</div> 

                <span class="priority-badge priority-${task.priority}">${task.priority}</span> 

            </div> 

            ${task.description ? `<div class="task-description">${escapeHtml(task.description)}</div>` : ''} 

            <div class="task-footer"> 

                <div>📅 ${task.due_date || 'No due date'}</div> 

                <div class="task-actions"> 

                    ${task.completed === 0 ?  

                        `<button class="complete-btn" onclick="toggleComplete(${task.id})">✓ Complete</button>` : 

                        `<button class="complete-btn" onclick="toggleComplete(${task.id})">↺ Undo</button>` 

                    } 

                    <button class="delete-btn" onclick="deleteTask(${task.id})">🗑 Delete</button> 

                </div> 

            </div> 

        </div> 

    `).join(''); 

} 

 

async function addTask(e) { 

    e.preventDefault(); 

    const title = document.getElementById('title').value; 

    if (!title.trim()) return alert('Enter a task title!'); 

     

    const newTask = { 

        title: title, 

        description: document.getElementById('description').value, 

        due_date: document.getElementById('dueDate').value, 

        priority: document.getElementById('priority').value 

    }; 

     

    try { 

        await fetch(`${API_BASE_URL}/tasks`, { 

            method: 'POST', 

            headers: { 'Content-Type': 'application/json' }, 

            body: JSON.stringify(newTask) 

        }); 

        document.getElementById('taskForm').reset(); 

        loadTasks(); 

    } catch (error) { 

        alert('Server offline!'); 

    } 

} 

 

async function toggleComplete(id) { 

    const task = tasks.find(t => t.id === id); 

    if (!task) return; 

     

    await fetch(`${API_BASE_URL}/tasks/${id}`, { 

        method: 'PUT', 

        headers: { 'Content-Type': 'application/json' }, 

        body: JSON.stringify({ completed: task.completed === 0 ? 1 : 0 }) 

    }); 

    loadTasks(); 

} 

 

async function deleteTask(id) { 

    if (!confirm('Delete this task?')) return; 

    await fetch(`${API_BASE_URL}/tasks/${id}`, { method: 'DELETE' }); 

    loadTasks(); 

} 

 

function escapeHtml(str) { 

    if (!str) return ''; 

    return str.replace(/[&<>]/g, m => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;' }[m])); 

} 
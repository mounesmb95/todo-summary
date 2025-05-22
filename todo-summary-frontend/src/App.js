import React, { useEffect, useState } from "react";

function App() {
  const [todos, setTodos] = useState([]);
  const [input, setInput] = useState({ title: "", description: "" });
  const [editId, setEditId] = useState(null);
  const [statusMessage, setStatusMessage] = useState("");

  const API_BASE = "http://localhost:8080";

  // Fetch todos on load
  useEffect(() => {
    fetchTodos();
  }, []);

  const fetchTodos = async () => {
    const res = await fetch(`${API_BASE}/todos`);
    const data = await res.json();
    setTodos(data);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.title || !input.description) return;

    const method = editId ? "PUT" : "POST";
    const endpoint = editId ? `${API_BASE}/todos/${editId}` : `${API_BASE}/todos`;

    await fetch(endpoint, {
      method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(input),
    });

    setInput({ title: "", description: "" });
    setEditId(null);
    fetchTodos();
  };

  const handleDelete = async (id) => {
    await fetch(`${API_BASE}/todos/${id}`, { method: "DELETE" });
    fetchTodos();
  };

  const handleEdit = (todo) => {
    setInput({ title: todo.title, description: todo.description });
    setEditId(todo.id);
  };

  const sendSummary = async () => {
    setStatusMessage("Sending summary...");
    try {
      const res = await fetch(`${API_BASE}/summarize`, { method: "POST" });
      const text = await res.text();
      setStatusMessage("✅ " + text);
    } catch (err) {
      setStatusMessage("❌ Failed to send summary.");
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: "auto", padding: 20 }}>
      <h1>Todo Summary Assistant</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: 20 }}>
        <input
          type="text"
          placeholder="Title"
          value={input.title}
          onChange={(e) => setInput({ ...input, title: e.target.value })}
          style={{ width: "100%", padding: 8, marginBottom: 8 }}
        />
        <textarea
          placeholder="Description"
          value={input.description}
          onChange={(e) => setInput({ ...input, description: e.target.value })}
          style={{ width: "100%", padding: 8, marginBottom: 8 }}
        />
        <button type="submit">{editId ? "Update Todo" : "Add Todo"}</button>
      </form>

      <h2>Todos</h2>
      <ul>
        {todos.map((todo) => (
          <li key={todo.id}>
            <strong>{todo.title}</strong> - {todo.description}
            <br />
            <button onClick={() => handleEdit(todo)}>Edit</button>
            <button onClick={() => handleDelete(todo.id)}>Delete</button>
          </li>
        ))}
      </ul>

      <div style={{ marginTop: 20 }}>
        <button onClick={sendSummary}>Generate & Send Summary</button>
        {statusMessage && <p>{statusMessage}</p>}
      </div>
    </div>
  );
}

export default App;

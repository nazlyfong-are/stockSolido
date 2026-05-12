document.querySelectorAll(".nav-btn").forEach(button => {
    button.addEventListener("click", () => {
        alert("Sección: " + button.textContent);
    });
});
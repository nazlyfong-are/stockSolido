document.querySelector("form").addEventListener("submit", function () {
    //feedback visual al usuario mientras se procesa el login
    const button = document.querySelector(".btn-login");

    if (button) {
        button.innerText  = "Ingresando...";
        button.disabled   = true;           // evita doble submit
        button.style.opacity = "0.8";
    }
    // El formulario se envia normalmente (POST /login --> Spring Security)
});
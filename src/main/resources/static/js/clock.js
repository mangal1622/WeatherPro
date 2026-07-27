function updateClock() {

    // Current UTC time
    const now = new Date();

    // Convert local time to UTC
    const utc = now.getTime() + (now.getTimezoneOffset() * 60000);

    // Apply city's timezone offset (in seconds)
    const cityTime = new Date(utc + (cityTimezone * 1000));

    const date = cityTime.toLocaleDateString("en-US", {
        weekday: "long",
        day: "numeric",
        month: "long"
    });

    const time = cityTime.toLocaleTimeString("en-US", {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true
    });

    document.getElementById("currentDate").textContent = date;
    document.getElementById("currentTime").textContent = time;
}

updateClock();

setInterval(updateClock, 1000);
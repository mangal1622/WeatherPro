document.addEventListener("DOMContentLoaded", () => {

    const currentPath = window.location.pathname;
    const isHomepage = currentPath === "/" || currentPath === "";

    if (isHomepage && navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;
                window.location.href = `/weather/location?lat=${encodeURIComponent(latitude)}&lon=${encodeURIComponent(longitude)}`;
            },
            (error) => {
                console.log("Geolocation denied or unavailable:", error.message);
            }
        );
    }

    const locationBtn = document.getElementById("locationBtn");

    locationBtn.addEventListener("click", () => {

        if (!navigator.geolocation) {
            alert("Geolocation is not supported by your browser.");
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {

                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;

                window.location.href =
                    `/weather/location?lat=${latitude}&lon=${longitude}`;

            },
            
            (error) => {

                alert("Unable to get your location.");

                console.error(error);

            }
        );

    });

});

document.addEventListener("DOMContentLoaded", () => {

    const hourlyScroll = document.getElementById("hourlyScroll");

    if (hourlyScroll) {

        let isDown = false;
        let startX;
        let scrollLeftStart;

        hourlyScroll.addEventListener("mousedown", (e) => {
            isDown = true;
            hourlyScroll.style.cursor = "grabbing";
            startX = e.pageX;
            scrollLeftStart = hourlyScroll.scrollLeft;
        });

        document.addEventListener("mouseup", () => {
            isDown = false;
            hourlyScroll.style.cursor = "grab";
        });

        document.addEventListener("mousemove", (e) => {
            if (!isDown) return;
            e.preventDefault();
            const dx = e.pageX - startX;
            hourlyScroll.scrollLeft = scrollLeftStart - dx;
        });

    }

});

document.addEventListener("DOMContentLoaded", () => {

    const cityInput = document.getElementById("citySearchInput");
    const suggestionsBox = document.getElementById("citySuggestions");
    const searchForm = cityInput ? cityInput.closest("form") : null;

    if (!cityInput || !suggestionsBox) return;

    let debounceTimer;

    cityInput.addEventListener("input", () => {

        const query = cityInput.value.trim();
        clearTimeout(debounceTimer);

        if (query.length === 0) {
            showDefaultSuggestions();
            return;
        }

        suggestionsBox.innerHTML = '<div class="suggestion-item">Searching...</div>';
        suggestionsBox.classList.add("active");

        debounceTimer = setTimeout(() => fetchSuggestions(query), 200);

    });

    cityInput.addEventListener("focus", () => {
        if (cityInput.value.trim().length === 0) {
            showDefaultSuggestions();
        }
    });

    
    function showDefaultSuggestions() {

        const cities = JSON.parse(localStorage.getItem("recentCities")) || [];

        suggestionsBox.innerHTML = "";

        const locationItem = document.createElement("div");
        locationItem.className = "suggestion-item suggestion-location";
        locationItem.innerHTML =
            '<i class="bi bi-crosshair"></i><span>Use my current location</span>';

        locationItem.addEventListener("click", () => {
            suggestionsBox.classList.remove("active");
            const locBtn = document.getElementById("locationBtn");
            if (locBtn) locBtn.click();
        });

        suggestionsBox.appendChild(locationItem);

        if (cities.length > 0) {

            const label = document.createElement("div");
            label.className = "suggestion-label";
            label.textContent = "Recent";
            suggestionsBox.appendChild(label);

            cities.forEach(city => {

                const item = document.createElement("div");
                item.className = "suggestion-item";
                item.innerHTML =
                    '<i class="bi bi-clock-history"></i><span>' + city + '</span>';

                item.addEventListener("click", () => {
                    cityInput.value = city;
                    suggestionsBox.classList.remove("active");
                    if (searchForm) searchForm.submit();
                });

                suggestionsBox.appendChild(item);

            });

        }

        suggestionsBox.classList.add("active");

    };

    function fetchSuggestions(query) {

        fetch("/weather/suggestions?query=" + encodeURIComponent(query))
            .then(res => res.json())
            .then(renderSuggestions)
            .catch(() => suggestionsBox.classList.remove("active"));

    }

    function renderSuggestions(cities) {

        suggestionsBox.innerHTML = "";

        if (!cities || cities.length === 0) {
            suggestionsBox.classList.remove("active");
            return;
        }

        cities.forEach(city => {

            const item = document.createElement("div");
            item.className = "suggestion-item";

            const displayLabel = [city.name, city.state, city.country]
                .filter(Boolean)
                .join(", ");

            const submitValue = city.country
                ? city.name + "," + city.country
                : city.name;

            item.innerHTML =
                '<i class="bi bi-geo-alt-fill"></i><span>' + displayLabel + '</span>';

            item.addEventListener("click", () => {
                cityInput.value = submitValue;
                suggestionsBox.classList.remove("active");
                if (searchForm) searchForm.submit();
            });

            suggestionsBox.appendChild(item);

        });

        suggestionsBox.classList.add("active");

    }

    document.addEventListener("click", (e) => {
        if (!suggestionsBox.contains(e.target) && e.target !== cityInput) {
            suggestionsBox.classList.remove("active");
        }
    });

    cityInput.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            suggestionsBox.classList.remove("active");
        }
    });

});
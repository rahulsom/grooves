import $ from 'jquery';
import * as bootstrap from 'bootstrap';

interface VersionsData {
    supported: string[];
    upcoming: string[];
    old: string[];
}

$(function () {
    function createVersion(name: string, recommended = false) {
        if (recommended) {
            return `<a class="dropdown-item" href="manual/${name}/index.html"><strong>${name}</strong></a>`;
        } else {
            return `<a class="dropdown-item" href="manual/${name}/index.html">${name}</a>`;
        }
    }

    $.getJSON('/versions.json', function (data: VersionsData) {
        $('#versions').html('');
        for (let i = 0; i < data.supported.length; i++) {
            $('#versions').append(createVersion(data.supported[i], i == 0));
        }
        $('#versions').append('<div class="dropdown-divider"></div>');
        $('#versions').append('<h6 class="dropdown-header">Upcoming</h6>');
        for (let i = 0; i < data.upcoming.length; i++) {
            $('#versions').append(createVersion(data.upcoming[i]));
        }
        $('#versions').append('<div class="dropdown-divider"></div>');
        $('#versions').append('<h6 class="dropdown-header">Old</h6>');
        for (let i = 0; i < data.old.length; i++) {
            $('#versions').append(createVersion(data.old[i]));
        }

        // Initialize Bootstrap dropdown after content is loaded
        const dropdownElement = document.getElementById('dropdownMenuLink');
        if (dropdownElement) {
            new bootstrap.Dropdown(dropdownElement);
        }

        console.log(data);
    });
});
function triggerFileInput() {
    document.getElementById('csvFileInput').click();
}
async function handleFileSelectAndUpload(event) {
    const pathData = document.getElementById('pathData');
    if (!pathData) {
        console.error('CRITICAL: pathData element not found in the DOM.');
        return;
    }
    const fileInput = event.target;

    if (fileInput.files.length === 0) {
        console.warn('No file selected.');
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file);
    console.log(`Preparing to upload: ${file.name}`);

    try {
        const basePath = pathData.dataset.basePath;
        const response = await fetch(basePath +'import', {
            method: 'POST',
            body: formData,
        });

        if (response.ok && response.redirected) {
            console.log(`Upload of "${file.name}" led to a redirect to ${response.url}. Page will refresh.`);
            alert(`"${file.name}" successfully uploaded`);
            window.location.reload();
            return;
        }

        const contentType = response.headers.get("content-type");
        let resultData = null;
        let parsingError = false;

        if (contentType && contentType.toLowerCase().includes("application/json")) {
            try {
                resultData = await response.json();
            } catch (e) {
                console.error("Failed to parse JSON response from server:", e);
                resultData = { serverMessage: "Error: Received malformed JSON from server." };
                parsingError = true;
            }
        } else {
            const textResponse = await response.text();
            resultData = { serverMessage: textResponse };
            if (contentType) {
                console.warn(`Received non-JSON response with Content-Type: ${contentType}`);
            } else {
                console.warn("Received response with no Content-Type. Treated as text.");
            }
            if (typeof textResponse === 'string' && (textResponse.trim().toLowerCase().startsWith("<!doctype html") || textResponse.trim().toLowerCase().startsWith("<html"))) {
                console.error("Server returned an HTML page, possibly an error page:", textResponse);
                resultData.serverMessage = "Server returned an HTML page (likely an error). Check console for details.";
            }
        }

        if (response.ok && !parsingError) {
            console.log('Upload successful (direct response):', resultData);
            alert(`Upload of "${file.name}" successful. Server says: ${resultData.serverMessage || 'Success.'}`);
            window.location.reload();
        } else {
            console.error('Upload failed. HTTP Status:', response.status, 'Response data:', resultData);
            let alertMessage = `Error uploading "${file.name}": `;
            if (resultData && resultData.serverMessage) {
                alertMessage += resultData.serverMessage;
            } else if (response.statusText) {
                alertMessage += response.statusText;
            } else if (parsingError) {
                alertMessage += "Malformed response from server.";
            }
            else {
                alertMessage += "Unknown error.";
            }
            alert(alertMessage);
        }
    } catch (networkError) {
        console.error('Upload network error:', networkError);
        alert(`A network error occurred while uploading "${file.name}". Please check your connection and try again.`);
    } finally {
        fileInput.value = '';
    }
}
document.addEventListener('DOMContentLoaded', () => {
    const csvFileInputElement = document.getElementById('csvFileInput');
    if (csvFileInputElement) {
        csvFileInputElement.addEventListener('change', handleFileSelectAndUpload);
    }

    const importButtonElement = document.getElementById('importCsvButton');
    if (importButtonElement) {
        importButtonElement.addEventListener('click', triggerFileInput);
    }
});
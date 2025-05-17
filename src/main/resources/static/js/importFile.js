function triggerFileInput() {
    document.getElementById('csvFileInput').click();
}

async function handleFileSelectAndUpload(event) {
    const fileInput = event.target;

    if (fileInput.files.length === 0) {
        console.warn('No file selected.');
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('csvFile', file);
    console.log(`Preparing to upload: ${file.name}`);
    try {
        const response = await fetch('/import', {
            method: 'POST',
            body: formData,
        });
        const result = await response.json();
        if (response.ok) {
            console.log('Upload successful:', result);
            alert(`Successfully uploaded "${file.name}".`);
        } else {
            console.error('Upload failed:', result);
            alert(`Error uploading "${file.name}": ${result.message || response.statusText || 'Unknown error.'}`);
        }
    } catch (error) {
        console.error('Upload error:', error);
        alert(`An error occurred during upload of "${file.name}". Check the console.`);
    } finally {
        fileInput.value = '';
    }
}
window.triggerFileInput = triggerFileInput;
window.handleFileSelectAndUpload = handleFileSelectAndUpload;
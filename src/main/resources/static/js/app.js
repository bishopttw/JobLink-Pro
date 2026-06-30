document.getElementById('loadJobsTrigger').addEventListener('click', function(e) {
    e.preventDefault();
    
    const heading = document.getElementById('feedHeading');
    const container = document.getElementById('dynamicContentContainer');
    
    heading.innerText = "Exploring Live Available Opportunities...";
    
    // Step A: Injection of Animated Skeleton Loaders 
    container.innerHTML = `
        <div class="skeleton-loader">
            <div class="skeleton-card"></div>
            <div class="skeleton-card"></div>
            <div class="skeleton-card"></div>
        </div>
    `;
    
    // Step B: Fetch from Backend JPA Rest Repository Endpoints
    // Simulating intentional 1.2-second connection interval latency for animation effect
    setTimeout(() => {
        fetch('/api/jobs')
            .then(response => response.json())
            .then(data => {
                container.innerHTML = ''; // Wipe out skeleton loaders
                
                if(data.length === 0) {
                    container.innerHTML = `<p>No jobs found inside Database Engine layers.</p>`;
                    return;
                }
                
                // Step C: Stream Database records cleanly inside Document Object Model
                data.forEach(job => {
                    const jobCard = document.createElement('div');
                    jobCard.classList.add('job-card');
                    jobCard.innerHTML = `
                        <div class="job-info">
                            <h3>${job.title}</h3>
                            <p><strong>${job.company}</strong></p>
                            <p>📍 ${job.location} | 💰 ${job.salary}</p>
                        </div>
                        <button class="apply-btn">Easy Apply</button>
                    `;
                    container.appendChild(jobCard);
                });
            })
            .catch(err => {
                container.innerHTML = `<p style="color:red;">Error connecting to Data Persistence layers.</p>`;
            });
    }, 1200);
});

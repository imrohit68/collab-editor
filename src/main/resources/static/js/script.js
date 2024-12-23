// Initialize UI elements
const status = document.getElementById('status');
const roomInfo = document.getElementById('room-info');
const canvas = document.getElementById('canvas');
const ctx = canvas.getContext('2d');
const colorPicker = document.getElementById('color-picker');
const brushSize = document.getElementById('brush-size');
const penTool = document.getElementById('pen-tool');
const eraserTool = document.getElementById('eraser-tool');
const clearCanvasBtn = document.getElementById('clear-canvas');
// Add these with your other initializations
let pc; // RTCPeerConnection instance
let localStream; // Local audio stream
let isCaller = false; // Track if this client initiated the call

// Extract roomId from URL
const urlParams = new URLSearchParams(window.location.search);
const roomId = urlParams.get('roomId') || 'default';
roomInfo.textContent = `Room ID: ${roomId}`;

// Canvas setup
function resizeCanvas() {
    const container = canvas.parentElement;
    canvas.width = container.clientWidth;
    canvas.height = container.clientHeight;
}

window.addEventListener('resize', resizeCanvas);
resizeCanvas();

// Drawing state
let isDrawing = false;
let currentTool = 'pen';

// Drawing functions
function startDrawing(e) {
    isDrawing = true;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    ctx.beginPath();
    ctx.moveTo(x, y);

    // Send initial position
    const drawAction = {
        type: currentTool,
        x: x,
        y: y,
        color: currentTool === 'eraser' ? '#ffffff' : colorPicker.value,
        size: brushSize.value,
        isStarting: true
    };
    stompClient.send(`/app/draw/${roomId}`, {}, JSON.stringify(drawAction));
}

function stopDrawing() {
    isDrawing = false;
    ctx.beginPath();
}

function draw(e) {
    if (!isDrawing) return;

    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.lineWidth = brushSize.value;
    ctx.strokeStyle = currentTool === 'eraser' ? '#ffffff' : colorPicker.value;

    ctx.lineTo(x, y);
    ctx.stroke();

    // Send drawing action
    const drawAction = {
        type: currentTool,
        x: x,
        y: y,
        color: ctx.strokeStyle,
        size: ctx.lineWidth,
        isStarting: false
    };

    stompClient.send(`/app/draw/${roomId}`, {}, JSON.stringify(drawAction));
}

// Tool selection
penTool.addEventListener('click', () => {
    currentTool = 'pen';
    penTool.classList.add('active');
    eraserTool.classList.remove('active');
});

eraserTool.addEventListener('click', () => {
    currentTool = 'eraser';
    eraserTool.classList.add('active');
    penTool.classList.remove('active');
});

clearCanvasBtn.addEventListener('click', () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    stompClient.send(`/app/draw/${roomId}`, {}, JSON.stringify({
        type: 'clear'
    }));
});

// Canvas event listeners
canvas.addEventListener('mousedown', startDrawing);
canvas.addEventListener('mousemove', draw);
canvas.addEventListener('mouseup', stopDrawing);
canvas.addEventListener('mouseout', stopDrawing);

// Initialize Ace Editor
const editor = ace.edit('editor');
editor.setTheme('ace/theme/monokai');
editor.session.setMode('ace/mode/java');
editor.setOptions({
    fontSize: 14,
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: true,
    showPrintMargin: false
});

// WebSocket setup
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);
stompClient.debug = null; // Disable debug logs

let isRemoteUpdate = false;

// Load the latest code and drawing when the user joins
async function loadLatestContent() {
    try {
        // Load code
        const codeResponse = await fetch(`/codeHistory/${roomId}`);
        const codeHistory = await codeResponse.json();
        if (codeHistory.length > 0) {
            editor.setValue(codeHistory[codeHistory.length - 1], -1);
        }

        // Load drawing with improved replay
        const drawingResponse = await fetch(`/drawingHistory/${roomId}`);
        const drawingHistory = await drawingResponse.json();

        let isNewPath = true;
        // Replay drawing actions
        drawingHistory.forEach(action => {
            const drawAction = JSON.parse(action);
            if (drawAction.type === 'clear') {
                ctx.clearRect(0, 0, canvas.width, canvas.height);
                isNewPath = true;
            } else {
                if (isNewPath || drawAction.isStarting) {
                    ctx.beginPath();
                    ctx.moveTo(drawAction.x, drawAction.y);
                    isNewPath = false;
                } else {
                    ctx.lineTo(drawAction.x, drawAction.y);
                }

                ctx.lineCap = 'round';
                ctx.lineJoin = 'round';
                ctx.lineWidth = drawAction.size;
                ctx.strokeStyle = drawAction.color;
                ctx.stroke();
            }
        });
    } catch (error) {
        console.error('Failed to load content:', error);
    }
}

// Connect to WebSocket
stompClient.connect({}, function() {
    console.log('Connected to WebSocket');
    status.textContent = 'Connected';
    status.classList.add('connected');

    // Subscribe to code updates
    stompClient.subscribe(`/topic/updates/${roomId}`, function(message) {
        if (!isRemoteUpdate) {
            isRemoteUpdate = true;
            const currentPosition = editor.getCursorPosition();
            const update = message.body;

            if (editor.getValue() !== update) {
                editor.setValue(update, -1);
                editor.moveCursorToPosition(currentPosition);
                editor.clearSelection();
            }

            isRemoteUpdate = false;
        }
    });

    // Subscribe to drawing updates
    stompClient.subscribe(`/topic/drawUpdates/${roomId}`, function(message) {
        const drawAction = JSON.parse(message.body);

        if (drawAction.type === 'clear') {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
        } else {
            if (drawAction.isStarting) {
                ctx.beginPath();
                ctx.moveTo(drawAction.x, drawAction.y);
            } else {
                ctx.lineTo(drawAction.x, drawAction.y);
            }

            ctx.lineCap = 'round';
            ctx.lineJoin = 'round';
            ctx.lineWidth = drawAction.size;
            ctx.strokeStyle = drawAction.color;
            ctx.stroke();
        }
    });
    stompClient.subscribe(`/topic/signal/${roomId}`, function (message) {
        const signal = JSON.parse(message.body);
        handleMessage(signal);
    });

    // Load the latest content when user joins
    loadLatestContent();

    // Handle editor changes
    let changeTimeout;
    editor.on('change', function() {
        if (!isRemoteUpdate) {
            clearTimeout(changeTimeout);
            changeTimeout = setTimeout(function() {
                const content = editor.getValue();
                if (content.trim() !== '') {
                    stompClient.send(`/app/edit/${roomId}`, {}, content);
                }
            }, 300);
        }
    });

}, function(error) {
    console.error('WebSocket Error:', error);
    status.textContent = 'Disconnected';
    status.classList.add('disconnected');
});
async function startCall() {
    isCaller = true; // Mark this tab as the caller

    try {
        // Capture local audio
        localStream = await navigator.mediaDevices.getUserMedia({
            audio: true,
            video: false
        });

        initializePeerConnection();

        // Create and send offer
        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);
        sendMessage({
            type: 'offer',
            sdp: offer.sdp
        });

    } catch (error) {
        console.error('Error starting call:', error);
    }
}

function initializePeerConnection() {
    pc = new RTCPeerConnection({
        iceServers: [{ urls: 'stun:stun.l.google.com:19302' }]
    });

    // Add local audio stream
    localStream.getTracks().forEach(track => pc.addTrack(track, localStream));

    // Handle incoming audio
    pc.ontrack = event => {
        const audio = new Audio();
        audio.srcObject = event.streams[0];
        audio.autoplay = true;
        document.body.appendChild(audio);
    };

    // Send ICE candidates
    pc.onicecandidate = event => {
        if (event.candidate) {
            sendMessage({
                type: 'candidate',
                candidate: event.candidate
            });
        }
    };
}

// Send messages via WebSocket
function sendMessage(message) {
    if (stompClient?.connected) {
        stompClient.send(`/app/signal/${roomId}`, {},JSON.stringify(message));
    }
}

// Handle incoming WebSocket messages
async function handleMessage(message) {
    try {
        if (message.type === 'offer' && !isCaller) {
            // Received offer, become the callee
            console.log('Received offer');
            await acceptCall(message);
        }
        else if (message.type === 'answer' && isCaller) {
            // Received answer
            console.log('Received answer');
            await pc.setRemoteDescription(new RTCSessionDescription({
                type: 'answer',
                sdp: message.sdp
            }));
        }
        else if (message.type === 'candidate' && pc) {
            // Add ICE candidate
            await pc.addIceCandidate(new RTCIceCandidate(message.candidate));
        }
    } catch (error) {
        console.error('Error handling message:', error);
    }
}

// Accept incoming call
async function acceptCall(offer) {
    try {
        // Capture local audio
        localStream = await navigator.mediaDevices.getUserMedia({
            audio: true,
            video: false
        });

        initializePeerConnection();

        // Set remote offer
        await pc.setRemoteDescription(new RTCSessionDescription({
            type: 'offer',
            sdp: offer.sdp
        }));

        // Create and send answer
        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);
        sendMessage({
            type: 'answer',
            sdp: answer.sdp
        });

    } catch (error) {
        console.error('Error accepting call:', error);
    }
}
console.log('=== API CONFIG DEBUG ===');
console.log('NODE_ENV:', process.env.NODE_ENV);
console.log('Production check:', process.env.NODE_ENV === 'production');

const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://ntu-add-drop-automator-v2-backend.onrender.com'  // Keep the v2!
  : 'http://localhost:5000';

console.log('Final API_BASE_URL:', API_BASE_URL);
console.log('=== END API CONFIG ===');

export default API_BASE_URL;
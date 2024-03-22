var counter = 0;
function click(e) {
    document.getElementById('clickTarget').innerText = 'You clicked me ' + ++counter + ' times!';
}
function load() {
    document.getElementById('clickSource').onclick = click;
}
window.onload = load;
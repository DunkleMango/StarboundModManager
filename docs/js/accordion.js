var acc = document.getElementsByClassName("accordion");

for (var i = 0; i < acc.length; i++) {
  acc[i].addEventListener("click", function() {
    this.classList.toggle("active");
    toggleAccordionContent(this);
  });
}

window.onload = toggleActiveAccordions();

function toggleActiveAccordions() {
  for (var i = 0; i < acc.length; i++) {
    if (acc[i].classList.contains("active")) {
      toggleAccordionContent(acc[i]);
    }
  }
}

function toggleAccordionContent(element) {
  var panel = element.nextElementSibling;
  if (panel.style.maxHeight){
    panel.style.maxHeight = null;
  } else {
    panel.style.maxHeight = panel.scrollHeight + "px";
  }
}

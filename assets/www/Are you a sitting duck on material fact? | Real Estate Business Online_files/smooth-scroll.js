(function($) {
    jQuery(document).ready(function($) {
        if(location.hash){
            $("html, body").animate({ scrollTop: $(location.hash).offset().top }, 500);
        }
        $(".scroll").click(function(event){ // When a link with the .scroll class is clicked
            event.preventDefault(); // Prevent the default action from occurring
            $('html,body').animate({scrollTop:$(this.hash).offset().top}, parseInt($(this).data("speed"))); // Animate the scroll to this link's href value
        });
    });
})(jQuery);
// Browser Slide-Show script. With image cross fade effect for those browsers
// that support it.
// Script copyright (C) 2004-2010 www.cryer.co.uk.
// Script is free to use provided this copyright header is included.

var FadeDurationMS = 1000;

function SetOpacity(object, opacityPct) {
    // IE.
    object.style.filter = 'alpha(opacity=' + opacityPct + ')';
    // Old mozilla and firefox
    object.style.MozOpacity = opacityPct / 100;
    // Everything else.
    object.style.opacity = opacityPct / 100;
}

function ChangeOpacity(id, msDuration, msStart, fromO, toO) {
    var element = document.getElementById(id);
    var msNow = (new Date()).getTime();
    var opacity = fromO + (toO - fromO) * (msNow - msStart) / msDuration;
    if (opacity >= 100)
    {
        SetOpacity(element, 100);
        element.timer = undefined;
    }
    else if (opacity <= 0)
    {
        SetOpacity(element, 0);
        element.timer = undefined;
    }
    else
    {
        SetOpacity(element, opacity);
        element.timer = window.setTimeout("ChangeOpacity('" + id + "'," + msDuration + "," + msStart + "," + fromO + "," + toO + ")", 10);
    }
}

function FadeInImage(foregroundID, newImage, backgroundID) {
    var foreground = document.getElementById(foregroundID);
    if (foreground.timer) window.clearTimeout(foreground.timer);
    if (backgroundID)
    {
        var background = document.getElementById(backgroundID);
        if (background)
        {
            if (background.src)
            {
                foreground.src = background.src;
                SetOpacity(foreground, 100);
            }
            background.src = newImage;
            background.style.backgroundImage = 'url(' + newImage + ')';
            background.style.backgroundRepeat = 'no-repeat';
            var startMS = (new Date()).getTime();
            foreground.timer = window.setTimeout("ChangeOpacity('" + foregroundID + "'," + FadeDurationMS + "," + startMS + ",100,0)", 10);
        }
    } else {
        foreground.src = newImage;
    }
}

var slideCache = new Array();

function RunSlideShow(pictureID, backgroundID, imageFiles, displaySecs) {
    var imageSeparator = imageFiles.indexOf(";");
    var nextImage = imageFiles.substring(0, imageSeparator);
    if (slideCache[nextImage] && slideCache[nextImage].loaded)
    {
        FadeInImage(pictureID, nextImage, backgroundID);
        var futureImages = imageFiles.substring(imageSeparator + 1, imageFiles.length)
        + ';' + nextImage;
        setTimeout("RunSlideShow('" + pictureID + "','" + backgroundID + "','" + futureImages + "'," + displaySecs + ")",
        displaySecs * 1000);
        // Identify the next image to cache.
        imageSeparator = futureImages.indexOf(";");
        nextImage = futureImages.substring(0, imageSeparator);
    } else {
        setTimeout("RunSlideShow('" + pictureID + "','" + backgroundID + "','" + imageFiles + "'," + displaySecs + ")", 250);
    }
    if (slideCache[nextImage] == null)
    {
        slideCache[nextImage] = new Image;
        slideCache[nextImage].loaded = false;
        slideCache[nextImage].onload = function() {
            this.loaded = true
        };
        slideCache[nextImage].src = nextImage;
    }
}
(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
var Spinner = require('spin');
var DateFormat = require('dateformat');

+ function ($) {
    'use strict';

    // UPLOAD CLASS DEFINITION
    // ======================

    var dropZone = document.getElementById('drop-zone');
    var uploadForm = document.getElementById('js-upload-form');
    var histTable = $('#genHistoryTable').DataTable({
        "paging":   false,
        "ordering": false,
        "info":     false,
        "searching": false
        });

    // var spinner = new Spinner().spin($('#myModalContent')[0]);
    // $(".overlay").fadeIn().append(spinner.el);
    // $(".overlay").fadeIn();

    var startUpload = function (files) {

        // https://coligo.io/building-ajax-file-uploader-with-node/

        if (files.length > 0) {
            // One or more files selected, process the file upload

            // create a FormData object which will be sent as the data payload in the
            // AJAX request
            var formData = new FormData();

            // loop through all the selected files
            for (var i = 0; i < files.length; i++) {
                var file = files[i];

                // add the files to formData object for the data payload
                formData.append('file', file, file.name);
                $.ajax({
                    url: '/uploadFile',
                    type: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        console.log('done');
                        histTable.row.add( [
                            data.company,    
                            DateFormat(new Date(data.generationTime), "yyyy-mm-dd HH:MM:ss"),
                            data.filename,
                            "<span class=\"btn btn-primary dlbtn\">Download<span>"
                        ]).draw(false);
                        $("#modalTitle").text("Generation Started");
                        $("#modalText").html("When complete. please supply password <b>" + data.password + "</b> to the file <b>" + data.filename + "</b>");
                        $("#myModal").modal("show");
                        // start processing
                        // spinner.stop();
                        // $(".overlay").fadeOut();
                        // window.location.href = "./out/yo2.docx";
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log('error');
                        $("#modalTitle").text("Upload Error");
                        $("#modalText").text("See Server logs for details. textStatus=" + textStatus + " errorThrown=" + errorThrown);
                        $("#myModal").modal("show");
                    }
                    /*xhr: function() {
                        // create an XMLHttpRequest
                        var xhr = new XMLHttpRequest();

                        // listen to the 'progress' event
                        xhr.upload.addEventListener('progress', function(evt) {

                        if (evt.lengthComputable) {
                            // calculate the percentage of upload completed
                            var percentComplete = evt.loaded / evt.total;
                            percentComplete = parseInt(percentComplete * 100);

                            // update the Bootstrap progress bar with the new percentage
                            $('.progress-bar').text(percentComplete + '%');
                            $('.progress-bar').width(percentComplete + '%');

                            // once the upload reaches 100%, set the progress bar text to done
                            if (percentComplete === 100) {
                                $('.progress-bar').html('Done');
                                // spinner.spin();
                                $(".overlay").fadeIn().append(spinner.el);
                                // new Spinner({color:'#999', lines: 12}).spin($(".overlay"));

                            }

                        }

                        }, false);

                        return xhr;
                    }*/
                });
            }


        }


    }

    uploadForm.addEventListener('submit', function (f) {
        var uploadFiles = document.getElementById('js-upload-files').files;
        f.preventDefault()
        startUpload(uploadFiles)
    })

    dropZone.ondrop = function (f) {
        // prevent brower from really opening the file
        f.preventDefault();
        this.className = 'upload-drop-zone';
        startUpload(f.dataTransfer.files)
    }

    dropZone.ondragover = function () {
        this.className = 'upload-drop-zone drop';   // change css
        return false;
    }

    dropZone.ondragleave = function () {
        this.className = 'upload-drop-zone';
        return false;
    }

    // We can attach the `fileselect` event to all file inputs on the page
    $(document).on('change', ':file', function () {
        var input = $(this),
            numFiles = input.get(0).files ? input.get(0).files.length : 1,
            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        input.trigger('fileselect', [numFiles, label]);
    });

    // We can watch for our custom `fileselect` event like this
    $(document).ready(function () {

        // put filename into text under input-group
        $(':file').on('fileselect', function (event, numFiles, label) {
            var input = $(this).parents('.input-group').find(':text'),
                log = numFiles > 1 ? numFiles + ' files selected' : label;

            if (input.length) {
                input.val(log);
            } else {
                if (log) alert(log);
            }
        });

        $.ajax({
            url: '/listFiles',
            type: 'GET',
            processData: false,
            contentType: false,
            success: function (dataArr) {
                $.each(dataArr, function(idx, data) {
                    console.log("file is " + data.filename);
                    histTable.row.add( [
                        data.company,    
                        DateFormat(new Date(data.generationTime), "yyyy-mm-dd HH:MM:ss"),
                        data.filename,
                        "<span class=\"btn btn-primary dlbtn\">Download<span>"
                    ]);
                });
                histTable.draw(false);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $("#modalTitle").text("List Error");
                $("#modalText").text("See Server logs for details. textStatus=" + textStatus + " errorThrown=" + errorThrown);
                $("#myModal").modal("show");
            }
        });
        
        
    });

    
    $(document).on('click', '#genHistoryTable .dlbtn', function () {
        startDownload($(this).closest("tr").children('td:nth-child(3)').text());
    } );

    var startDownload = function (filename) {
       // ajax doesn't handle file downloads elegantly
        var req = new XMLHttpRequest();
        req.open("POST", "/downloadFile", true);
        req.setRequestHeader("Content-Type", "application/json");
        req.responseType = "blob";
        req.onreadystatechange = function () {
          if (req.readyState === 4 && req.status === 200) {
            // test for IE
            if (typeof window.navigator.msSaveBlob === 'function') {
              window.navigator.msSaveBlob(req.response, "PdfName-" + new Date().getTime() + ".pdf");
            } else {
              var blob = req.response;
              var link = document.createElement('a');
              link.href = window.URL.createObjectURL(blob);
              link.download = filename;
              // append the link to the document body
              document.body.appendChild(link);
              link.click();
            }
          }
        };
        req.send(JSON.stringify({"filename" : filename }));
    }



}(jQuery);
},{"dateformat":2,"spin":3}],2:[function(require,module,exports){
/*
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */

(function(global) {
  'use strict';

  var dateFormat = (function() {
      var token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZWN]|'[^']*'|'[^']*'/g;
      var timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g;
      var timezoneClip = /[^-+\dA-Z]/g;
  
      // Regexes and supporting functions are cached through closure
      return function (date, mask, utc, gmt) {
  
        // You can't provide utc if you skip other args (use the 'UTC:' mask prefix)
        if (arguments.length === 1 && kindOf(date) === 'string' && !/\d/.test(date)) {
          mask = date;
          date = undefined;
        }
  
        date = date || new Date;
  
        if(!(date instanceof Date)) {
          date = new Date(date);
        }
  
        if (isNaN(date)) {
          throw TypeError('Invalid date');
        }
  
        mask = String(dateFormat.masks[mask] || mask || dateFormat.masks['default']);
  
        // Allow setting the utc/gmt argument via the mask
        var maskSlice = mask.slice(0, 4);
        if (maskSlice === 'UTC:' || maskSlice === 'GMT:') {
          mask = mask.slice(4);
          utc = true;
          if (maskSlice === 'GMT:') {
            gmt = true;
          }
        }
  
        var _ = utc ? 'getUTC' : 'get';
        var d = date[_ + 'Date']();
        var D = date[_ + 'Day']();
        var m = date[_ + 'Month']();
        var y = date[_ + 'FullYear']();
        var H = date[_ + 'Hours']();
        var M = date[_ + 'Minutes']();
        var s = date[_ + 'Seconds']();
        var L = date[_ + 'Milliseconds']();
        var o = utc ? 0 : date.getTimezoneOffset();
        var W = getWeek(date);
        var N = getDayOfWeek(date);
        var flags = {
          d:    d,
          dd:   pad(d),
          ddd:  dateFormat.i18n.dayNames[D],
          dddd: dateFormat.i18n.dayNames[D + 7],
          m:    m + 1,
          mm:   pad(m + 1),
          mmm:  dateFormat.i18n.monthNames[m],
          mmmm: dateFormat.i18n.monthNames[m + 12],
          yy:   String(y).slice(2),
          yyyy: y,
          h:    H % 12 || 12,
          hh:   pad(H % 12 || 12),
          H:    H,
          HH:   pad(H),
          M:    M,
          MM:   pad(M),
          s:    s,
          ss:   pad(s),
          l:    pad(L, 3),
          L:    pad(Math.round(L / 10)),
          t:    H < 12 ? dateFormat.i18n.timeNames[0] : dateFormat.i18n.timeNames[1],
          tt:   H < 12 ? dateFormat.i18n.timeNames[2] : dateFormat.i18n.timeNames[3],
          T:    H < 12 ? dateFormat.i18n.timeNames[4] : dateFormat.i18n.timeNames[5],
          TT:   H < 12 ? dateFormat.i18n.timeNames[6] : dateFormat.i18n.timeNames[7],
          Z:    gmt ? 'GMT' : utc ? 'UTC' : (String(date).match(timezone) || ['']).pop().replace(timezoneClip, ''),
          o:    (o > 0 ? '-' : '+') + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
          S:    ['th', 'st', 'nd', 'rd'][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10],
          W:    W,
          N:    N
        };
  
        return mask.replace(token, function (match) {
          if (match in flags) {
            return flags[match];
          }
          return match.slice(1, match.length - 1);
        });
      };
    })();

  dateFormat.masks = {
    'default':               'ddd mmm dd yyyy HH:MM:ss',
    'shortDate':             'm/d/yy',
    'mediumDate':            'mmm d, yyyy',
    'longDate':              'mmmm d, yyyy',
    'fullDate':              'dddd, mmmm d, yyyy',
    'shortTime':             'h:MM TT',
    'mediumTime':            'h:MM:ss TT',
    'longTime':              'h:MM:ss TT Z',
    'isoDate':               'yyyy-mm-dd',
    'isoTime':               'HH:MM:ss',
    'isoDateTime':           'yyyy-mm-dd\'T\'HH:MM:sso',
    'isoUtcDateTime':        'UTC:yyyy-mm-dd\'T\'HH:MM:ss\'Z\'',
    'expiresHeaderFormat':   'ddd, dd mmm yyyy HH:MM:ss Z'
  };

  // Internationalization strings
  dateFormat.i18n = {
    dayNames: [
      'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat',
      'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'
    ],
    monthNames: [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
      'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'
    ],
    timeNames: [
      'a', 'p', 'am', 'pm', 'A', 'P', 'AM', 'PM'
    ]
  };

function pad(val, len) {
  val = String(val);
  len = len || 2;
  while (val.length < len) {
    val = '0' + val;
  }
  return val;
}

/**
 * Get the ISO 8601 week number
 * Based on comments from
 * http://techblog.procurios.nl/k/n618/news/view/33796/14863/Calculate-ISO-8601-week-and-year-in-javascript.html
 *
 * @param  {Object} `date`
 * @return {Number}
 */
function getWeek(date) {
  // Remove time components of date
  var targetThursday = new Date(date.getFullYear(), date.getMonth(), date.getDate());

  // Change date to Thursday same week
  targetThursday.setDate(targetThursday.getDate() - ((targetThursday.getDay() + 6) % 7) + 3);

  // Take January 4th as it is always in week 1 (see ISO 8601)
  var firstThursday = new Date(targetThursday.getFullYear(), 0, 4);

  // Change date to Thursday same week
  firstThursday.setDate(firstThursday.getDate() - ((firstThursday.getDay() + 6) % 7) + 3);

  // Check if daylight-saving-time-switch occurred and correct for it
  var ds = targetThursday.getTimezoneOffset() - firstThursday.getTimezoneOffset();
  targetThursday.setHours(targetThursday.getHours() - ds);

  // Number of weeks between target Thursday and first Thursday
  var weekDiff = (targetThursday - firstThursday) / (86400000*7);
  return 1 + Math.floor(weekDiff);
}

/**
 * Get ISO-8601 numeric representation of the day of the week
 * 1 (for Monday) through 7 (for Sunday)
 * 
 * @param  {Object} `date`
 * @return {Number}
 */
function getDayOfWeek(date) {
  var dow = date.getDay();
  if(dow === 0) {
    dow = 7;
  }
  return dow;
}

/**
 * kind-of shortcut
 * @param  {*} val
 * @return {String}
 */
function kindOf(val) {
  if (val === null) {
    return 'null';
  }

  if (val === undefined) {
    return 'undefined';
  }

  if (typeof val !== 'object') {
    return typeof val;
  }

  if (Array.isArray(val)) {
    return 'array';
  }

  return {}.toString.call(val)
    .slice(8, -1).toLowerCase();
};



  if (typeof define === 'function' && define.amd) {
    define(function () {
      return dateFormat;
    });
  } else if (typeof exports === 'object') {
    module.exports = dateFormat;
  } else {
    global.dateFormat = dateFormat;
  }
})(this);

},{}],3:[function(require,module,exports){
//fgnass.github.com/spin.js#v1.2.5
/**
 * Copyright (c) 2011 Felix Gnass [fgnass at neteye dot de]
 * Licensed under the MIT license
 */

var prefixes = ['webkit', 'Moz', 'ms', 'O']; /* Vendor prefixes */
var animations = {}; /* Animation rules keyed by their name */
var useCssAnimations;

/**
 * Utility function to create elements. If no tag name is given,
 * a DIV is created. Optionally properties can be passed.
 */
function createEl(tag, prop) {
  var el = document.createElement(tag || 'div');
  var n;

  for(n in prop) {
    el[n] = prop[n];
  }
  return el;
}

/**
 * Appends children and returns the parent.
 */
function ins(parent /* child1, child2, ...*/) {
  for (var i=1, n=arguments.length; i<n; i++) {
    parent.appendChild(arguments[i]);
  }
  return parent;
}

/**
 * Insert a new stylesheet to hold the @keyframe or VML rules.
 */
var sheet = function() {
  var el = createEl('style');
  ins(document.getElementsByTagName('head')[0], el);
  return el.sheet || el.styleSheet;
}();

/**
 * Creates an opacity keyframe animation rule and returns its name.
 * Since most mobile Webkits have timing issues with animation-delay,
 * we create separate rules for each line/segment.
 */
function addAnimation(alpha, trail, i, lines) {
  var name = ['opacity', trail, ~~(alpha*100), i, lines].join('-');
  var start = 0.01 + i/lines*100;
  var z = Math.max(1-(1-alpha)/trail*(100-start) , alpha);
  var prefix = useCssAnimations.substring(0, useCssAnimations.indexOf('Animation')).toLowerCase();
  var pre = prefix && '-'+prefix+'-' || '';

  if (!animations[name]) {
    sheet.insertRule(
      '@' + pre + 'keyframes ' + name + '{' +
      '0%{opacity:'+z+'}' +
      start + '%{opacity:'+ alpha + '}' +
      (start+0.01) + '%{opacity:1}' +
      (start+trail)%100 + '%{opacity:'+ alpha + '}' +
      '100%{opacity:'+ z + '}' +
      '}', 0);
    animations[name] = 1;
  }
  return name;
}

/**
 * Tries various vendor prefixes and returns the first supported property.
 **/
function vendor(el, prop) {
  var s = el.style;
  var pp;
  var i;

  if(s[prop] !== undefined) return prop;
  prop = prop.charAt(0).toUpperCase() + prop.slice(1);
  for(i=0; i<prefixes.length; i++) {
    pp = prefixes[i]+prop;
    if(s[pp] !== undefined) return pp;
  }
}

/**
 * Sets multiple style properties at once.
 */
function css(el, prop) {
  for (var n in prop) {
    el.style[vendor(el, n)||n] = prop[n];
  }
  return el;
}

/**
 * Fills in default values.
 */
function merge(obj) {
  for (var i=1; i < arguments.length; i++) {
    var def = arguments[i];
    for (var n in def) {
      if (obj[n] === undefined) obj[n] = def[n];
    }
  }
  return obj;
}

/**
 * Returns the absolute page-offset of the given element.
 */
function pos(el) {
  var o = {x:el.offsetLeft, y:el.offsetTop};
  while((el = el.offsetParent)) {
    o.x+=el.offsetLeft;
    o.y+=el.offsetTop;
  }
  return o;
}

var defaults = {
  lines: 12,            // The number of lines to draw
  length: 7,            // The length of each line
  width: 5,             // The line thickness
  radius: 10,           // The radius of the inner circle
  rotate: 0,            // rotation offset
  color: '#000',        // #rgb or #rrggbb
  speed: 1,             // Rounds per second
  trail: 100,           // Afterglow percentage
  opacity: 1/4,         // Opacity of the lines
  fps: 20,              // Frames per second when using setTimeout()
  zIndex: 2e9,          // Use a high z-index by default
  className: 'spinner', // CSS class to assign to the element
  top: 'auto',          // center vertically
  left: 'auto'          // center horizontally
};

/** The constructor */
var Spinner = function Spinner(o) {
  if (!this.spin) return new Spinner(o);
  this.opts = merge(o || {}, Spinner.defaults, defaults);
};

Spinner.defaults = {};
merge(Spinner.prototype, {
  spin: function(target) {
    this.stop();
    var self = this;
    var o = self.opts;
    var el = self.el = css(createEl(0, {className: o.className}), {position: 'relative', zIndex: o.zIndex});
    var mid = o.radius+o.length+o.width;
    var ep; // element position
    var tp; // target position

    if (target) {
      target.insertBefore(el, target.firstChild||null);
      tp = pos(target);
      ep = pos(el);
      css(el, {
        left: (o.left == 'auto' ? tp.x-ep.x + (target.offsetWidth >> 1) : o.left+mid) + 'px',
        top: (o.top == 'auto' ? tp.y-ep.y + (target.offsetHeight >> 1) : o.top+mid)  + 'px'
      });
    }

    el.setAttribute('aria-role', 'progressbar');
    self.lines(el, self.opts);

    if (!useCssAnimations) {
      // No CSS animation support, use setTimeout() instead
      var i = 0;
      var fps = o.fps;
      var f = fps/o.speed;
      var ostep = (1-o.opacity)/(f*o.trail / 100);
      var astep = f/o.lines;

      !function anim() {
        i++;
        for (var s=o.lines; s; s--) {
          var alpha = Math.max(1-(i+s*astep)%f * ostep, o.opacity);
          self.opacity(el, o.lines-s, alpha, o);
        }
        self.timeout = self.el && setTimeout(anim, ~~(1000/fps));
      }();
    }
    return self;
  },
  stop: function() {
    var el = this.el;
    if (el) {
      clearTimeout(this.timeout);
      if (el.parentNode) el.parentNode.removeChild(el);
      this.el = undefined;
    }
    return this;
  },
  lines: function(el, o) {
    var i = 0;
    var seg;

    function fill(color, shadow) {
      return css(createEl(), {
        position: 'absolute',
        width: (o.length+o.width) + 'px',
        height: o.width + 'px',
        background: color,
        boxShadow: shadow,
        transformOrigin: 'left',
        transform: 'rotate(' + ~~(360/o.lines*i+o.rotate) + 'deg) translate(' + o.radius+'px' +',0)',
        borderRadius: (o.width>>1) + 'px'
      });
    }
    for (; i < o.lines; i++) {
      seg = css(createEl(), {
        position: 'absolute',
        top: 1+~(o.width/2) + 'px',
        transform: o.hwaccel ? 'translate3d(0,0,0)' : '',
        opacity: o.opacity,
        animation: useCssAnimations && addAnimation(o.opacity, o.trail, i, o.lines) + ' ' + 1/o.speed + 's linear infinite'
      });
      if (o.shadow) ins(seg, css(fill('#000', '0 0 4px ' + '#000'), {top: 2+'px'}));
      ins(el, ins(seg, fill(o.color, '0 0 1px rgba(0,0,0,.1)')));
    }
    return el;
  },
  opacity: function(el, i, val) {
    if (i < el.childNodes.length) el.childNodes[i].style.opacity = val;
  }
});

/////////////////////////////////////////////////////////////////////////
// VML rendering for IE
/////////////////////////////////////////////////////////////////////////

/**
 * Check and init VML support
 */
!function() {

  function vml(tag, attr) {
    return createEl('<' + tag + ' xmlns="urn:schemas-microsoft.com:vml" class="spin-vml">', attr);
  }

  var s = css(createEl('group'), {behavior: 'url(#default#VML)'});

  if (!vendor(s, 'transform') && s.adj) {

    // VML support detected. Insert CSS rule ...
    sheet.addRule('.spin-vml', 'behavior:url(#default#VML)');

    Spinner.prototype.lines = function(el, o) {
      var r = o.length+o.width;
      var s = 2*r;

      function grp() {
        return css(vml('group', {coordsize: s +' '+s, coordorigin: -r +' '+-r}), {width: s, height: s});
      }

      var margin = -(o.width+o.length)*2+'px';
      var g = css(grp(), {position: 'absolute', top: margin, left: margin});

      var i;

      function seg(i, dx, filter) {
        ins(g,
          ins(css(grp(), {rotation: 360 / o.lines * i + 'deg', left: ~~dx}),
            ins(css(vml('roundrect', {arcsize: 1}), {
                width: r,
                height: o.width,
                left: o.radius,
                top: -o.width>>1,
                filter: filter
              }),
              vml('fill', {color: o.color, opacity: o.opacity}),
              vml('stroke', {opacity: 0}) // transparent stroke to fix color bleeding upon opacity change
            )
          )
        );
      }

      if (o.shadow) {
        for (i = 1; i <= o.lines; i++) {
          seg(i, -2, 'progid:DXImageTransform.Microsoft.Blur(pixelradius=2,makeshadow=1,shadowopacity=.3)');
        }
      }
      for (i = 1; i <= o.lines; i++) seg(i);
      return ins(el, g);
    };
    Spinner.prototype.opacity = function(el, i, val, o) {
      var c = el.firstChild;
      o = o.shadow && o.lines || 0;
      if (c && i+o < c.childNodes.length) {
        c = c.childNodes[i+o]; c = c && c.firstChild; c = c && c.firstChild;
        if (c) c.opacity = val;
      }
    };
  }
  else {
    useCssAnimations = vendor(s, 'animation');
  }
}();

module.exports = Spinner;

},{}]},{},[1])
//# sourceMappingURL=bundle.js.map

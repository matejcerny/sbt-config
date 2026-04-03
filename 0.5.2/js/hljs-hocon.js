hljs.registerLanguage("hocon", function (hljs) {
  var SUBSTITUTION = {
    className: "subst",
    begin: /\$\{[?]?/,
    end: /\}/,
    contains: [
      {
        className: "variable",
        begin: /[a-zA-Z_][\w.-]*/,
      },
    ],
  };

  var TRIPLE_QUOTED_STRING = {
    className: "string",
    begin: /"""/,
    end: /"""/,
    contains: [SUBSTITUTION],
  };

  var QUOTED_STRING = {
    className: "string",
    begin: /"/,
    end: /"/,
    illegal: /\n/,
    contains: [hljs.BACKSLASH_ESCAPE, SUBSTITUTION],
  };

  return {
    aliases: ["conf"],
    contains: [
      hljs.HASH_COMMENT_MODE,
      hljs.C_LINE_COMMENT_MODE,
      {
        className: "keyword",
        begin: /\binclude\b/,
      },
      {
        className: "built_in",
        begin: /\b(?:file|url|classpath|required)\s*\(/,
        returnBegin: true,
        contains: [
          {
            className: "built_in",
            begin: /\b(?:file|url|classpath|required)/,
          },
          TRIPLE_QUOTED_STRING,
          QUOTED_STRING,
        ],
      },
      TRIPLE_QUOTED_STRING,
      QUOTED_STRING,
      SUBSTITUTION,
      {
        className: "literal",
        begin: /\b(?:true|false|null|on|off|yes|no)\b/,
      },
      {
        className: "number",
        begin: /\b-?\d+(\.\d+)?([eE][+-]?\d+)?\b/,
      },
      {
        className: "punctuation",
        begin: /[{}[\],]/,
      },
      {
        className: "attr",
        begin: /[\w.\-]+(?=\s*[=:{])/,
      },
      {
        className: "operator",
        begin: /[=:]|\+=/,
      },
    ],
  };
});

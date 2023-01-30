import React from "react";
import "./App.css";

import { Routes, Route } from "react-router-dom";
import styled, { ThemeProvider } from "styled-components";

import { darkTheme } from "./styles/themes/darkTheme";
import { lightTheme } from "./styles/themes/lightTheme";
import useTheme from "./styles/themes/useTheme";
import Itheme from "./styles/themes/theme";

function App() {
  const [themeMode, toggleTheme] = useTheme();
  const theme: Itheme = themeMode === "light" ? lightTheme : darkTheme;

  return (
    <ThemeProvider theme={theme}>
      <Routes>
        {/* 라우팅 될 페이지 */}
        {/* <Route path="/{path}" element={<{page} />}></Route> */}
      </Routes>
    </ThemeProvider>
  );
}

export default App;

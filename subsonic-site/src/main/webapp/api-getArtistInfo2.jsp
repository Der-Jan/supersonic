<%--
  ~ This file is part of Subsonic.
  ~
  ~  Subsonic is free software: you can redistribute it and/or modify
  ~  it under the terms of the GNU General Public License as published by
  ~  the Free Software Foundation, either version 3 of the License, or
  ~  (at your option) any later version.
  ~
  ~  Subsonic is distributed in the hope that it will be useful,
  ~  but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~  GNU General Public License for more details.
  ~
  ~  You should have received a copy of the GNU General Public License
  ~  along with Subsonic.  If not, see <http://www.gnu.org/licenses/>.
  ~
  ~  Copyright 2014 (C) Sindre Mehus
  --%>

<h2 class="div"><a name="getArtistInfo2"></a>getArtistInfo2</h2>

<p>
    <code>http://your-server/rest/getArtistInfo2.view</code>
    <br>Since <a href="#versions">1.11.0</a>
</p>

<p>
    Similar to <code>getArtistInfo</code>, but organizes music according to ID3 tags.
</p>
<table width="100%" class="bottomspace">
    <tr>
        <th class="param-heading">Parameter</th>
        <th class="param-heading">Required</th>
        <th class="param-heading">Default</th>
        <th class="param-heading">Comment</th>
    </tr>
    <tr class="table-altrow">
        <td><code>id</code></td>
        <td>Yes</td>
        <td></td>
        <td>The artist ID.</td>
    </tr>
    <tr>
        <td><code>count</code></td>
        <td>No</td>
        <td>20</td>
        <td>Max number of similar artists to return.</td>
    </tr>
</table>
<p>
    Returns a <code>&lt;subsonic-response&gt;</code> element with a nested <code>&lt;artistInfo2&gt;</code>
    element on success.
    <a href="inc/api/examples/artistInfo2_example_1.xml">Example</a>.
</p>
